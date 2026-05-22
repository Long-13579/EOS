package com.ces.eos.service.impl;

import com.ces.eos.constant.SortingConstants;
import com.ces.eos.dto.request.CreateTeamRequest;
import com.ces.eos.dto.request.GetTeamsRequest;
import com.ces.eos.dto.request.UpdateTeamRequest;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.dto.response.TeamResponse;
import com.ces.eos.dto.response.UserBaseResponse;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.User;
import com.ces.eos.enums.UserRole;
import com.ces.eos.exception.ResourceAlreadyExistsException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.TeamMapper;
import com.ces.eos.mapper.UserMapper;
import com.ces.eos.repository.TeamRepository;
import com.ces.eos.repository.UserRepository;
import com.ces.eos.service.TeamService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamServiceImpl implements TeamService {

  private final TeamRepository teamRepository;
  private final UserRepository userRepository;
  private final TeamMapper teamMapper;
  private final UserMapper userMapper;

  @Override
  public PagedEntityResponse<TeamResponse> getTeamsWithPagination(GetTeamsRequest request) {
    log.info(
        "action=getTeamsWithPagination.start page={} limit={}", request.page(), request.limit());
    Pageable pageable;
    if (request.page() == null && request.limit() == null) {
      pageable = Pageable.unpaged(SortingConstants.DEFAULT_ENTITIES_SORT);
    } else {
      pageable =
          PageRequest.of(
              request.page() - 1, request.limit(), SortingConstants.DEFAULT_ENTITIES_SORT);
    }

    log.debug(
        "action=getTeamsWithPagination.repo.findAll page={} limit={}",
        request.page(),
        request.limit());
    Page<Team> teamPage = teamRepository.findAll(pageable);

    Page<TeamResponse> responsePage = teamPage.map(teamMapper::toTeamResponseWithoutUsers);
    log.info("action=getTeamsWithPagination.success count={}", responsePage.getNumberOfElements());
    return PagedEntityResponse.from(responsePage);
  }

  @Override
  @Transactional
  public TeamResponse addTeam(CreateTeamRequest request) {
    log.info("action=addTeam.start");
    validateTeamName(request.name());

    Team team = teamMapper.toEntity(request);
    log.debug("action=addTeam.repo.save");
    Team savedTeam = teamRepository.save(team);
    log.info("action=addTeam.success teamId={}", savedTeam.getId());
    return teamMapper.toTeamResponse(savedTeam);
  }

  @Override
  @Transactional
  public TeamResponse updateTeam(UUID teamId, UpdateTeamRequest request) {
    log.info("action=updateTeam.start teamId={}", teamId);
    Team team = getTeamById(teamId);

    if (team.getName().equals(request.name())) {
      log.debug("action=updateTeam.branch.noChange teamId={}", teamId);
      log.info("action=updateTeam.success teamId={}", teamId);
      return teamMapper.toTeamResponse(team);
    }

    if (!team.getName().equalsIgnoreCase(request.name())) {
      log.debug("action=updateTeam.branch.validateName teamId={}", teamId);
      validateTeamName(request.name());
    }

    team.setName(request.name());

    log.debug("action=updateTeam.repo.save teamId={}", teamId);
    Team updatedTeam = teamRepository.save(team);
    log.info("action=updateTeam.success teamId={}", updatedTeam.getId());
    return teamMapper.toTeamResponse(updatedTeam);
  }

  @Override
  public List<TeamResponse> getTeamsByUserId(UUID userId, UserRole userRole) {
    log.debug("action=getTeamsByUserId.start userId={} role={}", userId, userRole);
    List<Team> teams;

    if (UserRole.ADMIN.equals(userRole)) {
      log.debug("action=getTeamsByUserId.branch.admin userId={}", userId);
      log.debug("action=getTeamsByUserId.repo.findAll");
      teams = teamRepository.findAll(SortingConstants.DEFAULT_ENTITIES_SORT);
    } else {
      log.debug("action=getTeamsByUserId.branch.user userId={}", userId);
      log.debug("action=getTeamsByUserId.repo.findAllByUsersId userId={}", userId);
      teams = teamRepository.findAllByUsers_Id(userId, SortingConstants.DEFAULT_ENTITIES_SORT);
    }

    List<TeamResponse> responses =
        teams.stream().map(teamMapper::toTeamResponseWithoutUsers).toList();

    log.debug("action=getTeamsByUserId.success count={}", responses.size());
    return responses;
  }

  @Override
  public List<UserBaseResponse> getUsersByTeamId(UUID teamId) {
    log.debug("action=getUsersByTeamId.start teamId={}", teamId);
    validateTeamExists(teamId);

    log.debug("action=getUsersByTeamId.repo.findUsersByTeamId teamId={}", teamId);
    List<User> users =
        userRepository.findUsersByTeamId(teamId, SortingConstants.DEFAULT_ENTITIES_SORT);

    List<UserBaseResponse> responses = users.stream().map(userMapper::toUserBaseResponse).toList();
    log.debug("action=getUsersByTeamId.success count={}", responses.size());
    return responses;
  }

  @Override
  public List<User> getActiveUsersByTeamId(UUID teamId) {
    log.debug("action=getActiveUsersByTeamId.start teamId={}", teamId);
    validateTeamExists(teamId);

    log.debug("action=getActiveUsersByTeamId.repo.findActiveUsersByTeamId teamId={}", teamId);
    List<User> users =
        userRepository.findActiveUsersByTeamId(teamId, SortingConstants.DEFAULT_ENTITIES_SORT);

    log.debug("action=getActiveUsersByTeamId.success count={}", users.size());
    return users;
  }

  private void validateTeamName(String name) {
    log.debug("action=validateTeamName.repo.existsByNameIgnoreCase");
    if (teamRepository.existsByNameIgnoreCase(name)) {
      log.warn("action=validateTeamName.validationFailed name={}", name);
      throw new ResourceAlreadyExistsException(
          Map.of("name", List.of(String.format("Team already exists: %s", name))));
    }
  }

  @Override
  public Team getTeamById(UUID teamId) {
    log.debug("action=getTeamById.start teamId={}", teamId);
    log.debug("action=getTeamById.repo.findById teamId={}", teamId);
    Team team =
        teamRepository
            .findById(teamId)
            .orElseThrow(
                () -> {
                  log.warn("action=getTeamById.validationFailed teamId={}", teamId);
                  return new ResourceNotFoundException(
                      Map.of(
                          "teamId", List.of(String.format("Team not found with id: %s", teamId))));
                });
    log.debug("action=getTeamById.success teamId={}", team.getId());
    return team;
  }

  @Override
  public void validateTeamExists(UUID teamId) {
    log.debug("action=validateTeamExists.start teamId={}", teamId);
    log.debug("action=validateTeamExists.repo.existsById teamId={}", teamId);
    if (!teamRepository.existsById(teamId)) {
      log.warn("action=validateTeamExists.validationFailed teamId={}", teamId);
      throw new ResourceNotFoundException(
          Map.of("teamId", List.of(String.format("Team not found with id: %s", teamId))));
    }
    log.debug("action=validateTeamExists.success teamId={}", teamId);
  }
}
