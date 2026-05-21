package com.ces.eos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ces.eos.dto.request.CreateUserRequest;
import com.ces.eos.dto.request.GetEntitiesRequest;
import com.ces.eos.dto.request.UpdateUserRequest;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.dto.response.UserResponse;
import com.ces.eos.entity.Role;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.User;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.enums.UserRole;
import com.ces.eos.exception.AuthException;
import com.ces.eos.exception.ResourceAlreadyExistsException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.UserMapper;
import com.ces.eos.repository.TeamRepository;
import com.ces.eos.repository.UserRepository;
import com.ces.eos.service.RoleService;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock private UserMapper userMapper;

  @Mock private RoleService roleService;

  @Mock private UserRepository userRepository;

  @Mock private TeamRepository teamRepository;

  @InjectMocks private UserServiceImpl userService;

  @Nested
  class GetUsersWithPagination {

    @Test
    void getUsersWithPagination_searchProvided_usesSearchUsers() {
      User user = org.mockito.Mockito.mock(User.class);
      UserResponse response =
          new UserResponse(
              UUID.randomUUID(),
              "John",
              "Doe",
              "john@company.com",
              "USER",
              true,
              null,
              null,
              null,
              null,
              Set.of());
      Page<User> page = new PageImpl<>(java.util.List.of(user));

      when(userRepository.searchUsers(
              org.mockito.ArgumentMatchers.eq("john"),
              any(org.springframework.data.domain.Pageable.class)))
          .thenReturn(page);
      when(userMapper.toUserResponse(user)).thenReturn(response);

      PagedEntityResponse<UserResponse> result =
          userService.getUsersWithPagination(new GetEntitiesRequest(1, 10, " john "));

      assertThat(result.data()).containsExactly(response);
      verify(userRepository)
          .searchUsers(
              org.mockito.ArgumentMatchers.eq("john"),
              any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void getUsersWithPagination_searchBlank_usesFindAll() {
      User user = org.mockito.Mockito.mock(User.class);
      UserResponse response =
          new UserResponse(
              UUID.randomUUID(),
              "John",
              "Doe",
              "john@company.com",
              "USER",
              true,
              null,
              null,
              null,
              null,
              Set.of());
      Page<User> page = new PageImpl<>(java.util.List.of(user));

      when(userRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
          .thenReturn(page);
      when(userMapper.toUserResponse(user)).thenReturn(response);

      PagedEntityResponse<UserResponse> result =
          userService.getUsersWithPagination(new GetEntitiesRequest(1, 10, "   "));

      assertThat(result.data()).containsExactly(response);
      verify(userRepository).findAll(any(org.springframework.data.domain.Pageable.class));
    }
  }

  @Nested
  class AddUser {

    @Test
    void addUser_uniqueEmailAndValidTeams_returnsSavedUserResponse() {
      UUID teamId = UUID.randomUUID();
      CreateUserRequest request =
          new CreateUserRequest(
              "John", "Doe", "john@company.com", "USER", new HashSet<>(Set.of(teamId)));
      User user = org.mockito.Mockito.mock(User.class);
      User saved = org.mockito.Mockito.mock(User.class);
      Role selectedRole = org.mockito.Mockito.mock(Role.class);
      Team team = org.mockito.Mockito.mock(Team.class);
      UserResponse response =
          new UserResponse(
              UUID.randomUUID(),
              "John",
              "Doe",
              "john@company.com",
              "USER",
              true,
              null,
              null,
              null,
              null,
              Set.of());

      when(userRepository.existsByEmail("john@company.com")).thenReturn(false);
      when(userMapper.toEntity(request)).thenReturn(user);
      when(roleService.getRoleByName("USER")).thenReturn(selectedRole);
      when(teamRepository.findAllByIdIn(Set.of(teamId))).thenReturn(Set.of(team));
      when(team.getId()).thenReturn(teamId);
      when(userRepository.save(user)).thenReturn(saved);
      when(userMapper.toUserResponse(saved)).thenReturn(response);

      UserResponse result = userService.addUser(request);

      assertThat(result).isEqualTo(response);
      verify(user).setRole(selectedRole);
      verify(user).setTeams(Set.of(team));
      verify(userRepository).save(user);
    }

    @Test
    void addUser_duplicateEmail_throwsResourceAlreadyExistsException() {
      CreateUserRequest request =
          new CreateUserRequest("John", "Doe", "john@company.com", "USER", Set.of());
      when(userRepository.existsByEmail("john@company.com")).thenReturn(true);

      assertThatThrownBy(() -> userService.addUser(request))
          .isInstanceOf(ResourceAlreadyExistsException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceAlreadyExistsException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_ALREADY_EXISTS));

      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addUser_partialTeamMatch_throwsResourceNotFoundException() {
      UUID validTeamId = UUID.randomUUID();
      UUID missingTeamId = UUID.randomUUID();
      CreateUserRequest request =
          new CreateUserRequest(
              "John",
              "Doe",
              "john@company.com",
              "USER",
              new HashSet<>(Set.of(validTeamId, missingTeamId)));
      User user = org.mockito.Mockito.mock(User.class);
      Role selectedRole = org.mockito.Mockito.mock(Role.class);
      Team validTeam = org.mockito.Mockito.mock(Team.class);

      when(userRepository.existsByEmail("john@company.com")).thenReturn(false);
      when(userMapper.toEntity(request)).thenReturn(user);
      when(roleService.getRoleByName("USER")).thenReturn(selectedRole);
      when(teamRepository.findAllByIdIn(Set.of(validTeamId, missingTeamId)))
          .thenReturn(Set.of(validTeam));
      when(validTeam.getId()).thenReturn(validTeamId);

      assertThatThrownBy(() -> userService.addUser(request))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));

      verify(userRepository, never()).save(any(User.class));
    }
  }

  @Nested
  class UpdateUser {

    @Test
    void updateUser_selfDemotionFromAdmin_throwsAuthException() {
      UUID userId = UUID.randomUUID();
      UpdateUserRequest request = new UpdateUserRequest("John", "Doe", "USER", Set.of());
      User existing = org.mockito.Mockito.mock(User.class);
      Role currentRole = org.mockito.Mockito.mock(Role.class);
      Role newRole = org.mockito.Mockito.mock(Role.class);

      when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
      when(roleService.getRoleByName("USER")).thenReturn(newRole);
      when(existing.getId()).thenReturn(userId);
      when(existing.getRole()).thenReturn(currentRole);
      when(currentRole.getName()).thenReturn(UserRole.ADMIN);
      when(newRole.getName()).thenReturn(UserRole.USER);

      assertThatThrownBy(() -> userService.updateUser(userId, userId, request))
          .isInstanceOf(AuthException.class)
          .satisfies(
              ex -> assertThat(((AuthException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_nonSelfRoleChange_allowsAdminDemotionOfOtherUser() {
      UUID actorId = UUID.randomUUID();
      UUID targetId = UUID.randomUUID();
      UpdateUserRequest request = new UpdateUserRequest("John", "Doe", "USER", Set.of());
      User existing = org.mockito.Mockito.mock(User.class);
      Role newRole = org.mockito.Mockito.mock(Role.class);
      User saved = org.mockito.Mockito.mock(User.class);
      UserResponse response =
          new UserResponse(
              targetId,
              "John",
              "Doe",
              "john@company.com",
              "USER",
              true,
              null,
              null,
              null,
              null,
              Set.of());

      when(userRepository.findById(targetId)).thenReturn(Optional.of(existing));
      when(roleService.getRoleByName("USER")).thenReturn(newRole);
      when(existing.getId()).thenReturn(targetId);
      when(newRole.getName()).thenReturn(UserRole.USER);
      when(userRepository.save(existing)).thenReturn(saved);
      when(userMapper.toUserResponse(saved)).thenReturn(response);

      UserResponse result = userService.updateUser(actorId, targetId, request);

      assertThat(result).isEqualTo(response);
      verify(userRepository).save(existing);
    }
  }

  @Nested
  class UserLookup {

    @Test
    void getUserById_missingId_throwsResourceNotFoundException() {
      UUID userId = UUID.randomUUID();
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userService.getUserById(userId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void getUserByIdAndTeamId_missingMembership_throwsResourceNotFoundException() {
      UUID userId = UUID.randomUUID();
      UUID teamId = UUID.randomUUID();
      when(userRepository.findByIdAndTeamId(userId, teamId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userService.getUserByIdAndTeamId(userId, teamId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }
  }
}
