package com.ces.eos.service.impl;

import com.ces.eos.dto.request.CreateRockRequest;
import com.ces.eos.dto.request.UpdateRockRequest;
import com.ces.eos.dto.response.RockListResponse;
import com.ces.eos.dto.response.RockResponse;
import com.ces.eos.dto.response.UserRockListResponse;
import com.ces.eos.entity.CustomYear;
import com.ces.eos.entity.Quarter;
import com.ces.eos.entity.Rock;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.User;
import com.ces.eos.enums.RockCategory;
import com.ces.eos.enums.RockStatus;
import com.ces.eos.exception.BadRequestException;
import com.ces.eos.exception.ConflictException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.RockMapper;
import com.ces.eos.repository.RockRepository;
import com.ces.eos.service.QuarterService;
import com.ces.eos.service.RockService;
import com.ces.eos.service.TeamService;
import com.ces.eos.service.UserService;
import com.ces.eos.service.YearService;
import com.ces.eos.util.DateUtils;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RockServiceImpl implements RockService {

  private final RockRepository rockRepository;
  private final RockMapper rockMapper;
  private final YearService yearService;
  private final QuarterService quarterService;
  private final TeamService teamService;
  private final UserService userService;

  @Override
  public RockListResponse getRocksByTeam(
      UUID teamId, UUID yearId, UUID quarterId, Boolean isArchived) {
    log.info(
        "action=getRocksByTeam.start teamId={} yearId={} quarterId={}", teamId, yearId, quarterId);
    validateReferences(yearId, quarterId);
    log.debug("action=getRocksByTeam.service.getTeamById teamId={}", teamId);
    Team team = teamService.getTeamById(teamId);

    log.debug(
        "action=getRocksByTeam.repo.findAllByTeamIdAndYearIdAndQuarterId teamId={} yearId={} quarterId={}",
        teamId,
        yearId,
        quarterId);
    List<Rock> rocks =
        rockRepository.findAllByTeamIdAndYearIdAndQuarterId(teamId, yearId, quarterId, isArchived);

    RockListResponse response = toRockListResponse(rocks, team);
    log.info("action=getRocksByTeam.success teamId={} count={}", teamId, rocks.size());
    return response;
  }

  private void validateReferences(UUID yearId, UUID quarterId) {
    log.debug("action=validateReferences.service.validateYearExists yearId={}", yearId);
    yearService.validateYearExists(yearId);
    log.debug("action=validateReferences.service.validateQuarterExists quarterId={}", quarterId);
    quarterService.validateQuarterExists(quarterId);
  }

  private RockListResponse toRockListResponse(List<Rock> rocks, Team team) {
    boolean isLeadership = isLeadershipTeam(team);

    EnumMap<RockCategory, List<RockResponse>> groupedRocks = new EnumMap<>(RockCategory.class);
    groupedRocks.put(RockCategory.COMPANY, new ArrayList<>());
    groupedRocks.put(RockCategory.DEPARTMENT, new ArrayList<>());
    groupedRocks.put(RockCategory.INDIVIDUAL, new ArrayList<>());

    for (Rock rock : rocks) {
      RockCategory category = rock.getCategory();
      if (category == null) {
        log.warn(
            "action=toRockListResponse.validationFailed reason=nullCategory rockId={}",
            rock.getId());
        throw new IllegalStateException(
            String.format("Data integrity violation: rock %s has null category", rock.getId()));
      }
      if (!isLeadership && category == RockCategory.COMPANY) {
        log.debug("action=toRockListResponse.branch.skipCompanyRock rockId={}", rock.getId());
        continue;
      }
      RockResponse response = rockMapper.toRockResponse(rock);
      groupedRocks.computeIfAbsent(category, k -> new ArrayList<>()).add(response);
    }

    return new RockListResponse(
        isLeadership ? groupedRocks.get(RockCategory.COMPANY) : null,
        groupedRocks.get(RockCategory.DEPARTMENT),
        groupedRocks.get(RockCategory.INDIVIDUAL));
  }

  @Override
  @Transactional
  public RockResponse addRock(CreateRockRequest request, UUID creatorId) {
    log.info("action=addRock.start creatorId={} teamId={}", creatorId, request.teamId());
    Rock rock = rockMapper.toEntity(request);
    log.debug("action=addRock.service.getTeamById teamId={}", request.teamId());
    Team team = teamService.getTeamById(request.teamId());
    validateCompanyRockBelongsToLeadership(team, rock.getCategory());

    log.debug("action=addRock.service.getUserById creatorId={}", creatorId);
    User creator = userService.getUserById(creatorId);
    rock.setCreatedBy(creator);
    rock.setUpdatedBy(creator);

    rock.setTeam(team);

    log.debug("action=addRock.service.getQuarterById quarterId={}", request.quarterId());
    Quarter quarter = quarterService.getQuarterById(request.quarterId());
    rock.setQuarter(quarter);

    log.debug("action=addRock.service.getOrCreateYear year={}", request.year());
    CustomYear customYear = yearService.getOrCreateYear(request.year());
    rock.setYear(customYear);

    validateDueDate(request.dueDate(), quarter, request.year());

    log.debug(
        "action=addRock.service.getUserByIdAndTeamId ownerId={} teamId={}",
        request.ownerId(),
        request.teamId());
    rock.setOwner(userService.getUserByIdAndTeamId(request.ownerId(), request.teamId()));

    log.debug("action=addRock.repo.save teamId={}", request.teamId());
    Rock savedRock = rockRepository.save(rock);

    log.info("action=addRock.success rockId={}", savedRock.getId());
    return rockMapper.toRockResponse(savedRock);
  }

  @Override
  @Transactional
  public RockResponse updateRockArchiveStatus(UUID rockId, Boolean isArchived) {
    log.info("action=updateRockArchiveStatus.start rockId={} isArchived={}", rockId, isArchived);
    Rock rock = getRockById(rockId);
    validateCompanyRockBelongsToLeadership(rock.getTeam(), rock.getCategory());

    if (Objects.equals(rock.getIsArchived(), isArchived)) {
      log.debug("action=updateRockArchiveStatus.branch.noChange rockId={}", rockId);
      log.info("action=updateRockArchiveStatus.success rockId={}", rockId);
      return rockMapper.toRockResponse(rock);
    }

    rock.setIsArchived(isArchived);
    log.debug("action=updateRockArchiveStatus.repo.save rockId={}", rockId);
    Rock updatedRock = rockRepository.save(rock);
    log.info("action=updateRockArchiveStatus.success rockId={}", updatedRock.getId());
    return rockMapper.toRockResponse(updatedRock);
  }

  @Override
  @Transactional
  public RockResponse updateRock(UUID rockId, UpdateRockRequest request, UUID updaterId) {
    log.info("action=updateRock.start rockId={} updaterId={}", rockId, updaterId);
    Rock rock = getRockById(rockId);
    validateCompanyRockBelongsToLeadership(rock.getTeam(), rock.getCategory());

    if (Boolean.TRUE.equals(rock.getIsArchived())) {
      log.warn("action=updateRock.validationFailed reason=archived rockId={}", rockId);
      throw new ConflictException(
          Map.of("rockId", List.of("Cannot update an archived rock. Please unarchive it first.")));
    }
    log.debug("action=updateRock.service.getQuarterById quarterId={}", request.quarterId());
    Quarter quarter = quarterService.getQuarterById(request.quarterId());
    validateDueDate(request.dueDate(), quarter, request.year());
    validateCompanyRockBelongsToLeadership(
        rock.getTeam(), RockCategory.valueOf(request.category()));
    UUID teamId = rock.getTeam().getId();
    log.debug(
        "action=updateRock.service.getUserByIdAndTeamId ownerId={} teamId={}",
        request.ownerId(),
        teamId);
    User owner = userService.getUserByIdAndTeamId(request.ownerId(), teamId);
    log.debug("action=updateRock.service.getOrCreateYear year={}", request.year());
    CustomYear year = yearService.getOrCreateYear(request.year());
    log.debug("action=updateRock.service.getUserById updaterId={}", updaterId);
    User updater = userService.getUserById(updaterId);
    rockMapper.updateRockFromRequest(rock, request, owner, year, quarter, updater);
    log.debug("action=updateRock.repo.save rockId={}", rockId);
    Rock savedRock = rockRepository.save(rock);
    log.info("action=updateRock.success rockId={}", savedRock.getId());
    return rockMapper.toRockResponse(savedRock);
  }

  private void validateDueDate(Instant dueDate, Quarter quarter, Integer year) {
    if (!DateUtils.isDateWithinQuarterAndYear(dueDate, quarter, year)) {
      log.warn(
          "action=validateDueDate.validationFailed quarter={} year={}", quarter.getName(), year);
      throw new BadRequestException(
          Map.of(
              "dueDate",
              List.of(
                  String.format(
                      "Due date '%s' must fall within the specified quarter (%s) for year %d. "
                          + "Quarter runs from %s to %s.",
                      dueDate,
                      quarter.getName(),
                      year,
                      DateUtils.fromMonthDayToString(quarter.getStartDate()),
                      DateUtils.fromMonthDayToString(quarter.getEndDate())))));
    }
  }

  @Override
  public Rock getRockById(UUID rockId) {
    log.debug("action=getRockById.start rockId={}", rockId);
    log.debug("action=getRockById.repo.findById rockId={}", rockId);
    Rock rock =
        rockRepository
            .findById(rockId)
            .orElseThrow(
                () -> {
                  log.warn("action=getRockById.validationFailed rockId={}", rockId);
                  return new ResourceNotFoundException(
                      Map.of(
                          "rockId", List.of(String.format("Rock not found with id: %s", rockId))));
                });
    log.debug("action=getRockById.success rockId={}", rock.getId());
    return rock;
  }

  @Override
  @Transactional
  public RockResponse updateRockStatus(UUID rockId, RockStatus newStatus) {
    log.info("action=updateRockStatus.start rockId={} status={}", rockId, newStatus);
    Rock rock = getRockById(rockId);
    validateCompanyRockBelongsToLeadership(rock.getTeam(), rock.getCategory());
    if (Objects.equals(rock.getStatus(), newStatus)) {
      log.debug("action=updateRockStatus.branch.noChange rockId={} status={}", rockId, newStatus);
      log.info("action=updateRockStatus.success rockId={}", rockId);
      return rockMapper.toRockResponse(rock);
    }

    rock.setStatus(newStatus);
    log.debug("action=updateRockStatus.repo.save rockId={}", rockId);
    Rock updatedRock = rockRepository.save(rock);
    log.info("action=updateRockStatus.success rockId={}", updatedRock.getId());
    return rockMapper.toRockResponse(updatedRock);
  }

  private boolean isLeadershipTeam(Team team) {
    return Boolean.TRUE.equals(team.getIsLeadership());
  }

  private void validateCompanyRockBelongsToLeadership(Team team, RockCategory category) {
    if (category == RockCategory.COMPANY && !isLeadershipTeam(team)) {
      log.warn(
          "action=validateCompanyRockBelongsToLeadership.validationFailed teamId={} category={}",
          team.getId(),
          category);
      throw new BadRequestException(
          Map.of("teamId", List.of("Company rocks can only belong to the Leadership team.")));
    }
  }

  @Override
  public UserRockListResponse findActiveRocksByOwnerId(UUID ownerId, UUID yearId, UUID quarterId) {
    log.info(
        "action=findActiveRocksByOwnerId.start ownerId={} yearId={} quarterId={}",
        ownerId,
        yearId,
        quarterId);
    validateReferences(yearId, quarterId);
    log.debug(
        "action=findActiveRocksByOwnerId.repo.findActiveRocksByOwnerIdAndYearAndQuarter ownerId={} yearId={} quarterId={}",
        ownerId,
        yearId,
        quarterId);
    UserRockListResponse response =
        new UserRockListResponse(
            rockRepository
                .findActiveRocksByOwnerIdAndYearAndQuarter(ownerId, yearId, quarterId)
                .stream()
                .map(rockMapper::toRockResponse)
                .toList());
    log.info(
        "action=findActiveRocksByOwnerId.success ownerId={} count={}",
        ownerId,
        response.data().size());
    return response;
  }
}
