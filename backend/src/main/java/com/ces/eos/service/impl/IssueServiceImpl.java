package com.ces.eos.service.impl;

import com.ces.eos.constant.IssueTypeConstants;
import com.ces.eos.constant.SortingConstants;
import com.ces.eos.dto.request.CreateIssueRequest;
import com.ces.eos.dto.request.PaginationRequest;
import com.ces.eos.dto.request.UpdateIssueRequest;
import com.ces.eos.dto.response.IssueResponse;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.entity.Issue;
import com.ces.eos.exception.BadRequestException;
import com.ces.eos.exception.ConflictException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.IssueMapper;
import com.ces.eos.repository.IssueRepository;
import com.ces.eos.service.IssueService;
import com.ces.eos.service.IssueTypeService;
import com.ces.eos.service.TeamService;
import com.ces.eos.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IssueServiceImpl implements IssueService {

  private final IssueRepository issueRepository;
  private final IssueMapper issueMapper;

  private final TeamService teamService;
  private final UserService userService;
  private final IssueTypeService issueTypeService;

  @Override
  @Transactional
  public IssueResponse addIssue(CreateIssueRequest request, UUID creatorId) {
    log.info("action=addIssue.start creatorId={} teamId={}", creatorId, request.teamId());
    Issue issue = issueMapper.toEntity(request);

    if (request.issueTypeId() != null) {
      log.debug("action=addIssue.service.getIssueTypeById issueTypeId={}", request.issueTypeId());
      issue.setIssueType(issueTypeService.getIssueTypeById(request.issueTypeId()));
    }

    // Set creator in memory for response mapping.
    // The actual created_by column is populated by @CreatedBy auditing.
    log.debug("action=addIssue.service.getUserById creatorId={}", creatorId);
    issue.setCreator(userService.getUserById(creatorId));

    log.debug("action=addIssue.service.getTeamById teamId={}", request.teamId());
    issue.setTeam(teamService.getTeamById(request.teamId()));

    log.debug("action=addIssue.repo.save teamId={}", request.teamId());
    Issue savedIssue = issueRepository.save(issue);
    log.info("action=addIssue.success issueId={}", savedIssue.getId());
    return issueMapper.toIssueResponse(savedIssue);
  }

  @Override
  public PagedEntityResponse<IssueResponse> getIssuesByTeam(
      UUID teamId, PaginationRequest request, String issueTypeId, Boolean isArchived) {
    log.info(
        "action=getIssuesByTeam.start teamId={} page={} limit={}",
        teamId,
        request.page(),
        request.limit());
    log.debug("action=getIssuesByTeam.service.validateTeamExists teamId={}", teamId);
    teamService.validateTeamExists(teamId);

    Pageable pageable =
        PageRequest.of(request.page() - 1, request.limit(), SortingConstants.DEFAULT_ENTITIES_SORT);

    boolean hasTypeFilter = issueTypeId != null && !issueTypeId.isBlank();
    log.debug(
        "action=getIssuesByTeam.branch.hasTypeFilter teamId={} hasTypeFilter={}",
        teamId,
        hasTypeFilter);

    Page<UUID> issueIdsPage =
        hasTypeFilter
            ? fetchFilteredIssueIdsByType(teamId, issueTypeId, isArchived, pageable)
            : fetchIssueIdsByArchivePolicy(teamId, isArchived, pageable);

    if (issueIdsPage.isEmpty()) {
      log.debug("action=getIssuesByTeam.branch.emptyPage teamId={}", teamId);
      log.info("action=getIssuesByTeam.success teamId={} count=0", teamId);
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    log.debug(
        "action=getIssuesByTeam.repo.findAllByIdIn count={}", issueIdsPage.getContent().size());
    Map<UUID, Issue> issueMap =
        issueRepository.findAllByIdIn(issueIdsPage.getContent()).stream()
            .collect(Collectors.toMap(Issue::getId, Function.identity()));

    List<IssueResponse> issueResponses =
        issueIdsPage.getContent().stream()
            .peek(
                id -> {
                  if (!issueMap.containsKey(id)) {
                    log.warn(
                        "action=getIssuesByTeam.validationFailed reason=missingEntityForId issueId={}",
                        id);
                  }
                })
            .map(issueMap::get)
            .filter(Objects::nonNull)
            .map(issueMapper::toIssueResponse)
            .toList();

    log.info("action=getIssuesByTeam.success teamId={} count={}", teamId, issueResponses.size());
    return PagedEntityResponse.from(
        new PageImpl<>(issueResponses, pageable, issueIdsPage.getTotalElements()));
  }

  @Override
  @Transactional
  public void deleteIssueById(UUID issueId) {
    log.info("action=deleteIssueById.start issueId={}", issueId);
    Issue issue = getIssueById(issueId);
    log.debug("action=deleteIssueById.repo.delete issueId={}", issueId);
    issueRepository.delete(issue);
    log.info("action=deleteIssueById.success issueId={}", issueId);
  }

  @Override
  @Transactional
  public IssueResponse updateIssue(UUID issueId, UpdateIssueRequest request) {
    log.info("action=updateIssue.start issueId={}", issueId);
    Issue issue = getIssueById(issueId);

    if (Boolean.TRUE.equals(issue.getIsArchived())) {
      log.warn("action=updateIssue.validationFailed reason=archived issueId={}", issueId);
      throw new ConflictException(
          Map.of(
              "issueId", List.of("Cannot update an archived issue. Please unarchive it first.")));
    }

    issue.setTitle(request.title());
    issue.setDescription(request.description());

    if (request.issueTypeId() != null) {
      log.debug("action=updateIssue.branch.issueTypeSet issueTypeId={}", request.issueTypeId());
      issue.setIssueType(issueTypeService.getIssueTypeById(request.issueTypeId()));
    } else {
      log.debug("action=updateIssue.branch.issueTypeCleared issueId={}", issueId);
      issue.setIssueType(null);
    }

    log.debug("action=updateIssue.repo.save issueId={}", issueId);
    Issue updatedIssue = issueRepository.save(issue);
    log.info("action=updateIssue.success issueId={}", updatedIssue.getId());
    return issueMapper.toIssueResponse(updatedIssue);
  }

  @Override
  @Transactional
  public IssueResponse updateIssueArchiveStatus(UUID issueId, Boolean isArchived) {
    log.info("action=updateIssueArchiveStatus.start issueId={} isArchived={}", issueId, isArchived);
    Issue issue = getIssueById(issueId);
    if (Objects.equals(issue.getIsArchived(), isArchived)) {
      log.debug("action=updateIssueArchiveStatus.branch.noChange issueId={}", issueId);
      log.info("action=updateIssueArchiveStatus.success issueId={}", issueId);
      return issueMapper.toIssueResponse(issue);
    }

    issue.setIsArchived(isArchived);
    log.debug("action=updateIssueArchiveStatus.repo.save issueId={}", issueId);
    Issue updatedIssue = issueRepository.save(issue);
    log.info("action=updateIssueArchiveStatus.success issueId={}", updatedIssue.getId());
    return issueMapper.toIssueResponse(updatedIssue);
  }

  private Issue getIssueById(UUID issueId) {
    log.debug("action=getIssueById.repo.findById issueId={}", issueId);
    return issueRepository
        .findById(issueId)
        .orElseThrow(
            () -> {
              log.warn("action=getIssueById.validationFailed issueId={}", issueId);
              return new ResourceNotFoundException(
                  Map.of(
                      "issueId", List.of(String.format("Issue not found with id: %s", issueId))));
            });
  }

  private UUID parseIssueTypeId(String issueTypeId) {
    try {
      return UUID.fromString(issueTypeId);
    } catch (IllegalArgumentException e) {
      log.warn("action=parseIssueTypeId.validationFailed issueTypeId={}", issueTypeId);
      throw new BadRequestException(
          Map.of("issueTypeId", List.of("issueTypeId must be a valid UUID or 'none'")));
    }
  }

  private Page<UUID> fetchFilteredIssueIdsByType(
      UUID teamId, String issueTypeId, Boolean isArchived, Pageable pageable) {
    if (IssueTypeConstants.NONE_ISSUE_TYPE.equalsIgnoreCase(issueTypeId)) {
      log.debug("action=fetchFilteredIssueIdsByType.branch.noneType teamId={}", teamId);
      return issueRepository.findIssueIdsByTeamIdAndIssueTypeIsNull(teamId, isArchived, pageable);
    }

    log.debug(
        "action=fetchFilteredIssueIdsByType.branch.specificType teamId={} issueTypeId={}",
        teamId,
        issueTypeId);
    UUID parsedId = parseIssueTypeId(issueTypeId);
    return issueRepository.findIssueIdsByTeamIdAndIssueTypeId(
        teamId, parsedId, isArchived, pageable);
  }

  private Page<UUID> fetchIssueIdsByArchivePolicy(
      UUID teamId, Boolean isArchived, Pageable pageable) {
    if (Boolean.TRUE.equals(isArchived)) {
      log.debug("action=fetchIssueIdsByArchivePolicy.branch.includeArchived teamId={}", teamId);
      return issueRepository.findIssueIdsByTeamId(teamId, isArchived, pageable);
    }
    // Unarchived — exclude long-term (business rule)
    log.debug("action=fetchIssueIdsByArchivePolicy.branch.excludeLongTerm teamId={}", teamId);
    return fetchIssuesExcludingLongTerm(teamId, isArchived, pageable);
  }

  private Page<UUID> fetchIssuesExcludingLongTerm(
      UUID teamId, Boolean isArchived, Pageable pageable) {
    // Looking up by name guarantees a fast failure (Exception) if the constant is out of sync with
    // the DB.
    log.debug(
        "action=fetchIssuesExcludingLongTerm.service.getIssueTypeByName name={}",
        IssueTypeConstants.LONG_TERM_ISSUE_TYPE);
    UUID longTermIssueTypeId =
        issueTypeService.getIssueTypeByName(IssueTypeConstants.LONG_TERM_ISSUE_TYPE).getId();

    log.debug(
        "action=fetchIssuesExcludingLongTerm.repo.findIssueIdsByTeamIdExcludingIssueTypeId teamId={} issueTypeId={}",
        teamId,
        longTermIssueTypeId);
    return issueRepository.findIssueIdsByTeamIdExcludingIssueTypeId(
        teamId, isArchived, longTermIssueTypeId, pageable);
  }
}
