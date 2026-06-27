package com.ces.eos.service.impl;

import com.ces.eos.constant.SortingConstants;
import com.ces.eos.dto.request.CreateUserRequest;
import com.ces.eos.dto.request.GetEntitiesRequest;
import com.ces.eos.dto.request.UpdateUserRequest;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.dto.response.UserResponse;
import com.ces.eos.entity.Role;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.User;
import com.ces.eos.enums.UserRole;
import com.ces.eos.exception.AuthException;
import com.ces.eos.exception.ConflictException;
import com.ces.eos.exception.ResourceAlreadyExistsException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.UserMapper;
import com.ces.eos.repository.RefreshTokenRepository;
import com.ces.eos.repository.TeamRepository;
import com.ces.eos.repository.UserRepository;
import com.ces.eos.service.RoleService;
import com.ces.eos.service.TeamMembershipValidationService;
import com.ces.eos.service.UserDeactivationValidator;
import com.ces.eos.service.UserService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
public class UserServiceImpl implements UserService {

  private final UserMapper userMapper;
  private final RoleService roleService;
  private final UserRepository userRepository;
  private final TeamRepository teamRepository;
  private final TeamMembershipValidationService teamMembershipValidationService;
  private final UserDeactivationValidator userDeactivationValidator;
  private final RefreshTokenRepository refreshTokenRepository;

  @Override
  public PagedEntityResponse<UserResponse> getUsersWithPagination(GetEntitiesRequest request) {
    log.info(
        "action=getUsersWithPagination.start page={} limit={}", request.page(), request.limit());
    Pageable pageable =
        PageRequest.of(request.page() - 1, request.limit(), SortingConstants.DEFAULT_ENTITIES_SORT);

    Page<User> userPage;

    String search = request.search();
    if (search != null && !search.trim().isEmpty()) {
      log.debug("action=getUsersWithPagination.branch.searchApplied");
      log.debug(
          "action=getUsersWithPagination.repo.searchUsers page={} limit={}",
          request.page(),
          request.limit());
      userPage = userRepository.searchUsers(search.trim(), pageable);
    } else {
      log.debug("action=getUsersWithPagination.branch.searchNotApplied");
      log.debug(
          "action=getUsersWithPagination.repo.findAll page={} limit={}",
          request.page(),
          request.limit());
      userPage = userRepository.findAll(pageable);
    }

    Page<UserResponse> responsePage = userPage.map(userMapper::toUserResponse);
    log.info("action=getUsersWithPagination.success count={}", responsePage.getNumberOfElements());
    return PagedEntityResponse.from(responsePage);
  }

  @Override
  @Transactional
  public UserResponse addUser(CreateUserRequest request) {
    log.info("action=addUser.start");
    validateEmailNotExists(request.email());

    User user = userMapper.toEntity(request);
    log.debug("action=addUser.service.getRoleByName role={}", request.role());
    user.setRole(roleService.getRoleByName(request.role()));

    assignUserToTeams(user, request.teamIds());

    log.debug("action=addUser.repo.save");
    User savedUser = userRepository.save(user);
    log.info("action=addUser.success userId={}", savedUser.getId());
    return userMapper.toUserResponse(savedUser);
  }

  @Override
  @Transactional
  public UserResponse updateUser(UUID currentUserId, UUID userId, UpdateUserRequest user) {
    log.info("action=updateUser.start userId={} actorId={}", userId, currentUserId);
    User existingUser = getUserById(userId);

    log.debug("action=updateUser.service.getRoleByName role={}", user.role());
    Role newRole = roleService.getRoleByName(user.role());
    preventSelfDemotion(currentUserId, existingUser, newRole.getName());

    existingUser.setFirstName(user.firstName());
    existingUser.setLastName(user.lastName());
    existingUser.setRole(newRole);
    assignUserToTeams(existingUser, user.teamIds());

    log.debug("action=updateUser.repo.save userId={}", userId);
    User updatedUser = userRepository.save(existingUser);
    log.info("action=updateUser.success userId={}", updatedUser.getId());
    return userMapper.toUserResponse(updatedUser);
  }

  @Override
  public User getUserById(UUID userId) {
    log.debug("action=getUserById.start userId={}", userId);
    log.debug("action=getUserById.repo.findById userId={}", userId);
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.warn("action=getUserById.validationFailed userId={}", userId);
                  return new ResourceNotFoundException(
                      Map.of(
                          "userId", List.of(String.format("User not found with id: %s", userId))));
                });
    log.debug("action=getUserById.success userId={}", user.getId());
    return user;
  }

  @Override
  @Transactional
  public UserResponse deactivateUser(UUID currentUserId, UUID userId) {
    log.info("action=deactivateUser.start userId={} actorId={}", userId, currentUserId);

    if (currentUserId.equals(userId)) {
      log.warn("action=deactivateUser.selfDeactivationBlocked userId={}", userId);
      throw AuthException.forbidden("Admins cannot deactivate themselves.");
    }

    User user = getUserById(userId);

    if (!user.getIsActive()) {
      log.warn("action=deactivateUser.alreadyInactive userId={}", userId);
      throw new ConflictException(Map.of("userId", List.of("User is already inactive.")));
    }

    userDeactivationValidator.validate(userId);

    user.setIsActive(false);
    userRepository.save(user);

    refreshTokenRepository.deleteByUserId(userId);

    log.info("action=deactivateUser.success userId={}", userId);
    return userMapper.toUserResponse(user);
  }

  @Override
  @Transactional
  public UserResponse activateUser(UUID currentUserId, UUID userId) {
    log.info("action=activateUser.start userId={} actorId={}", userId, currentUserId);

    User user = getUserById(userId);

    if (user.getIsActive()) {
      log.warn("action=activateUser.alreadyActive userId={}", userId);
      throw new ConflictException(Map.of("userId", List.of("User is already active.")));
    }

    user.setIsActive(true);
    userRepository.save(user);

    log.info("action=activateUser.success userId={}", userId);
    return userMapper.toUserResponse(user);
  }

  @Override
  public User getUserByIdAndTeamId(UUID userId, UUID teamId) {
    log.debug("action=getUserByIdAndTeamId.start userId={} teamId={}", userId, teamId);
    log.debug(
        "action=getUserByIdAndTeamId.repo.findByIdAndTeamId userId={} teamId={}", userId, teamId);
    User user =
        userRepository
            .findByIdAndTeamId(userId, teamId)
            .orElseThrow(
                () -> {
                  log.warn(
                      "action=getUserByIdAndTeamId.validationFailed userId={} teamId={}",
                      userId,
                      teamId);
                  return new ResourceNotFoundException(
                      Map.of(
                          "userId",
                          List.of(
                              String.format(
                                  "User with id %s is not part of team with id %s",
                                  userId, teamId))));
                });
    log.debug("action=getUserByIdAndTeamId.success userId={}", user.getId());
    return user;
  }

  private void assignUserToTeams(User user, Set<UUID> teamIds) {
    Set<Team> currentTeams = user.getTeams();
    Set<UUID> currentTeamIds =
        currentTeams != null
            ? currentTeams.stream().map(Team::getId).collect(Collectors.toSet())
            : new HashSet<>();

    Set<UUID> removedTeamIds = new HashSet<>(currentTeamIds);
    if (teamIds != null) {
      removedTeamIds.removeAll(teamIds);
    }

    if (!removedTeamIds.isEmpty()) {
      log.debug(
          "action=assignUserToTeams.validateRemoval count={}", removedTeamIds.size());
      List<String> allViolations = new ArrayList<>();
      for (UUID removedTeamId : removedTeamIds) {
        try {
          teamMembershipValidationService.validateUserTeamRemoval(user.getId(), removedTeamId);
        } catch (ConflictException e) {
          List<String> teamViolations = e.getDetails().get("teamRemoval");
          if (teamViolations != null) {
            allViolations.addAll(teamViolations);
          }
        }
      }
      if (!allViolations.isEmpty()) {
        throw new ConflictException(
            "Cannot remove user from team(s). Reassign responsibilities first.",
            Map.of("teamRemoval", allViolations));
      }
    }

    if (teamIds == null || teamIds.isEmpty()) {
      log.debug("action=assignUserToTeams.branch.emptyTeamIds");
      user.setTeams(new HashSet<>());
      return;
    }

    log.debug("action=assignUserToTeams.repo.findAllByIdIn requestedCount={}", teamIds.size());
    Set<Team> teams = teamRepository.findAllByIdIn(teamIds);
    Set<UUID> foundTeamIds = teams.stream().map(Team::getId).collect(Collectors.toSet());

    teamIds.removeAll(foundTeamIds);

    if (!teamIds.isEmpty()) {
      log.warn("action=assignUserToTeams.validationFailed invalidCount={}", teamIds.size());
      throw new ResourceNotFoundException(
          Map.of(
              "teamIds", teamIds.stream().map(id -> "Invalid team id: " + id.toString()).toList()));
    }

    user.setTeams(teams);
  }

  private void validateEmailNotExists(String email) {
    log.debug("action=validateEmailNotExists.repo.existsByEmail");
    if (userRepository.existsByEmail(email)) {
      log.warn("action=validateEmailNotExists.validationFailed");
      throw new ResourceAlreadyExistsException(
          Map.of("email", List.of(String.format("Email already exists: %s", email))));
    }
  }

  private void preventSelfDemotion(UUID actorUserId, User targetUser, UserRole newRole) {
    // Early return if the user is editing someone else
    if (!actorUserId.equals(targetUser.getId())) {
      log.debug("action=preventSelfDemotion.branch.notSelf");
      return;
    }

    // Prevent an Admin from changing their own role to anything else
    if (targetUser.getRole().getName() == UserRole.ADMIN && newRole != UserRole.ADMIN) {
      log.warn("action=preventSelfDemotion.validationFailed actorUserId={}", actorUserId);
      throw AuthException.forbidden("Admins cannot demote themselves.");
    }
  }
}
