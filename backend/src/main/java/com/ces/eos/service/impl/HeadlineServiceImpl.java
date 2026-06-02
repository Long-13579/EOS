package com.ces.eos.service.impl;

import com.ces.eos.constant.SortingConstants;
import com.ces.eos.dto.request.CreateHeadlineRequest;
import com.ces.eos.dto.request.PaginationRequest;
import com.ces.eos.dto.request.UpdateHeadlineRequest;
import com.ces.eos.dto.response.HeadlineResponse;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.entity.Headline;
import com.ces.eos.entity.User;
import com.ces.eos.exception.ConflictException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.HeadlineMapper;
import com.ces.eos.repository.HeadlineRepository;
import com.ces.eos.service.HeadlineService;
import com.ces.eos.service.L10MeetingChangeLogService;
import com.ces.eos.service.TeamService;
import com.ces.eos.service.UserService;
import tools.jackson.databind.ObjectMapper;
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
public class HeadlineServiceImpl implements HeadlineService {

  private final HeadlineRepository headlineRepository;
  private final HeadlineMapper headlineMapper;
  private final TeamService teamService;
  private final UserService userService;
  private final L10MeetingChangeLogService l10MeetingChangeLogService;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public HeadlineResponse createHeadline(CreateHeadlineRequest request, UUID creatorId) {
    log.info("action=createHeadline.start creatorId={} teamId={}", creatorId, request.teamId());
    Headline headline = headlineMapper.toEntity(request);
    log.debug("action=createHeadline.service.getTeamById teamId={}", request.teamId());
    headline.setTeam(teamService.getTeamById(request.teamId()));
    // TODO: Improve performance by using a JPA reference or auditing mechanism to populate
    // createdBy and updatedBy, instead of loading the full User entity from the database and
    // assigning it manually.
    log.debug("action=createHeadline.service.getUserById creatorId={}", creatorId);
    User creator = userService.getUserById(creatorId);
    headline.setCreatedBy(creator);
    headline.setUpdatedBy(creator);

    log.debug("action=createHeadline.repo.save teamId={}", request.teamId());
    Headline savedHeadline = headlineRepository.save(headline);
    
    // Log the new headline creation
    log.debug("action=createHeadline.logChange headlineId={}", savedHeadline.getId());
    l10MeetingChangeLogService.logChange(
        request.teamId(),
        "HEADLINE",
        savedHeadline.getId(),
        null,
        objectMapper.valueToTree(headlineMapper.toHeadlineResponse(savedHeadline)).toString());
    
    log.info("action=createHeadline.success headlineId={}", savedHeadline.getId());
    return headlineMapper.toHeadlineResponse(savedHeadline);
  }

  @Override
  public PagedEntityResponse<HeadlineResponse> getHeadlinesByTeam(
      UUID teamId, PaginationRequest request, Boolean isArchived) {
    log.info(
        "action=getHeadlinesByTeam.start teamId={} page={} limit={}",
        teamId,
        request.page(),
        request.limit());
    teamService.validateTeamExists(teamId);

    Pageable pageable =
        PageRequest.of(request.page() - 1, request.limit(), SortingConstants.DEFAULT_ENTITIES_SORT);

    log.debug("action=getHeadlinesByTeam.repo.findHeadlineIdsByTeamId teamId={}", teamId);
    Page<UUID> headlineIdsPage =
        headlineRepository.findHeadlineIdsByTeamId(teamId, isArchived, pageable);

    if (headlineIdsPage.isEmpty()) {
      log.debug("action=getHeadlinesByTeam.branch.emptyPage teamId={}", teamId);
      log.info("action=getHeadlinesByTeam.success teamId={} count=0", teamId);
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    log.debug(
        "action=getHeadlinesByTeam.repo.findAllByIdIn count={}",
        headlineIdsPage.getContent().size());
    Map<UUID, Headline> headlineMap =
        headlineRepository.findAllByIdIn(headlineIdsPage.getContent()).stream()
            .collect(Collectors.toMap(Headline::getId, Function.identity()));

    List<HeadlineResponse> headlineResponses =
        headlineIdsPage.getContent().stream()
            .peek(
                id -> {
                  if (!headlineMap.containsKey(id)) {
                    log.warn(
                        "action=getHeadlinesByTeam.validationFailed reason=missingEntityForId headlineId={}",
                        id);
                  }
                })
            .map(headlineMap::get)
            .filter(Objects::nonNull)
            .map(headlineMapper::toHeadlineResponse)
            .toList();

    log.info(
        "action=getHeadlinesByTeam.success teamId={} count={}", teamId, headlineResponses.size());
    return PagedEntityResponse.from(
        new PageImpl<>(headlineResponses, pageable, headlineIdsPage.getTotalElements()));
  }

  @Override
  @Transactional
  public HeadlineResponse updateHeadline(
      UUID headlineId, UpdateHeadlineRequest request, UUID updaterId) {
    log.info("action=updateHeadline.start headlineId={} updaterId={}", headlineId, updaterId);
    Headline headline = getHeadlineById(headlineId);

    if (Boolean.TRUE.equals(headline.getIsArchived())) {
      log.warn("action=updateHeadline.validationFailed reason=archived headlineId={}", headlineId);
      throw new ConflictException(
          Map.of(
              "headlineId",
              List.of("Cannot update an archived headline. Please unarchive it first.")));
    }

    // Capture before snapshot
    var beforeSnapshot = objectMapper.valueToTree(headlineMapper.toHeadlineResponse(headline)).toString();

    headline.setTitle(request.title());

    log.debug("action=updateHeadline.service.getUserById updaterId={}", updaterId);
    User updater = userService.getUserById(updaterId);
    headline.setUpdatedBy(updater);

    log.debug("action=updateHeadline.repo.save headlineId={}", headlineId);
    Headline savedHeadline = headlineRepository.save(headline);
    
    // Log the headline update
    log.debug("action=updateHeadline.logChange headlineId={}", savedHeadline.getId());
    l10MeetingChangeLogService.logChange(
        headline.getTeam().getId(),
        "HEADLINE",
        savedHeadline.getId(),
        beforeSnapshot,
        objectMapper.valueToTree(headlineMapper.toHeadlineResponse(savedHeadline)).toString());
    
    log.info("action=updateHeadline.success headlineId={}", savedHeadline.getId());
    return headlineMapper.toHeadlineResponse(savedHeadline);
  }

  @Override
  @Transactional
  public HeadlineResponse updateHeadlineArchiveStatus(UUID headlineId, Boolean isArchived) {
    log.info(
        "action=updateHeadlineArchiveStatus.start headlineId={} isArchived={}",
        headlineId,
        isArchived);
    Headline headline = getHeadlineById(headlineId);

    if (Objects.equals(headline.getIsArchived(), isArchived)) {
      log.debug("action=updateHeadlineArchiveStatus.branch.noChange headlineId={}", headlineId);
      log.info("action=updateHeadlineArchiveStatus.success headlineId={}", headlineId);
      return headlineMapper.toHeadlineResponse(headline);
    }

    // Capture before snapshot
    var beforeSnapshot = objectMapper.valueToTree(headlineMapper.toHeadlineResponse(headline)).toString();

    headline.setIsArchived(isArchived);
    log.debug("action=updateHeadlineArchiveStatus.repo.save headlineId={}", headlineId);
    Headline updatedHeadline = headlineRepository.save(headline);
    
    // Log the archive status change
    log.debug("action=updateHeadlineArchiveStatus.logChange headlineId={}", updatedHeadline.getId());
    l10MeetingChangeLogService.logChange(
        headline.getTeam().getId(),
        "HEADLINE",
        updatedHeadline.getId(),
        beforeSnapshot,
        objectMapper.valueToTree(headlineMapper.toHeadlineResponse(updatedHeadline)).toString());
    
    log.info("action=updateHeadlineArchiveStatus.success headlineId={}", updatedHeadline.getId());
    return headlineMapper.toHeadlineResponse(updatedHeadline);
  }

  @Override
  @Transactional
  public void deleteHeadline(UUID headlineId) {
    log.info("action=deleteHeadline.start headlineId={}", headlineId);
    Headline headline = getHeadlineById(headlineId);
    
    // Log the headline deletion
    log.debug("action=deleteHeadline.logChange headlineId={}", headline.getId());
    l10MeetingChangeLogService.logChange(
        headline.getTeam().getId(),
        "HEADLINE",
        headline.getId(),
        objectMapper.valueToTree(headlineMapper.toHeadlineResponse(headline)).toString(),
        null);
    
    log.debug("action=deleteHeadline.repo.delete headlineId={}", headlineId);
    headlineRepository.delete(headline);
    log.info("action=deleteHeadline.success headlineId={}", headlineId);
  }

  private Headline getHeadlineById(UUID headlineId) {
    log.debug("action=getHeadlineById.repo.findById headlineId={}", headlineId);
    return headlineRepository
        .findById(headlineId)
        .orElseThrow(
            () -> {
              log.warn("action=getHeadlineById.validationFailed headlineId={}", headlineId);
              return new ResourceNotFoundException(
                  Map.of(
                      "headlineId",
                      List.of(String.format("Headline not found with id: %s", headlineId))));
            });
  }
}
