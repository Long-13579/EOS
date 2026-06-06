package com.ces.eos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ces.eos.dto.request.CreateTeamRequest;
import com.ces.eos.dto.request.GetTeamsRequest;
import com.ces.eos.dto.request.UpdateTeamRequest;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.dto.response.TeamResponse;
import com.ces.eos.dto.response.UserBaseResponse;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.User;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.enums.UserRole;
import com.ces.eos.exception.ResourceAlreadyExistsException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.TeamMapper;
import com.ces.eos.mapper.UserMapper;
import com.ces.eos.repository.HeadlineRepository;
import com.ces.eos.repository.IssueRepository;
import com.ces.eos.repository.L10MeetingRepository;
import com.ces.eos.repository.MetricRepository;
import com.ces.eos.repository.MetricValueRepository;
import com.ces.eos.repository.RockRepository;
import com.ces.eos.repository.TeamRepository;
import com.ces.eos.repository.TodoRepository;
import com.ces.eos.repository.UserRepository;
import java.util.List;
import java.util.Optional;
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
class TeamServiceImplTest {

  @Mock private TeamRepository teamRepository;

  @Mock private UserRepository userRepository;

  @Mock private TodoRepository todoRepository;

  @Mock private IssueRepository issueRepository;

  @Mock private HeadlineRepository headlineRepository;

  @Mock private MetricRepository metricRepository;

  @Mock private MetricValueRepository metricValueRepository;

  @Mock private RockRepository rockRepository;

  @Mock private L10MeetingRepository l10MeetingRepository;

  @Mock private TeamMapper teamMapper;

  @Mock private UserMapper userMapper;

  @InjectMocks private TeamServiceImpl teamService;

  @Nested
  class GetTeamsWithPagination {

    @Test
    void getTeamsWithPagination_dataExists_returnsPagedResponse() {
      Team team = org.mockito.Mockito.mock(Team.class);
      TeamResponse mapped =
          new TeamResponse(UUID.randomUUID(), "Engineering", false, null, null, null, null, null);
      Page<Team> page = new PageImpl<>(List.of(team));

      when(teamRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
          .thenReturn(page);
      when(teamMapper.toTeamResponseWithoutUsers(team)).thenReturn(mapped);

      PagedEntityResponse<TeamResponse> result =
          teamService.getTeamsWithPagination(new GetTeamsRequest(1, 10));

      assertThat(result.data()).containsExactly(mapped);
      verify(teamRepository).findAll(any(org.springframework.data.domain.Pageable.class));
    }
  }

  @Nested
  class AddTeam {

    @Test
    void addTeam_uniqueName_savesAndReturnsResponse() {
      CreateTeamRequest request = new CreateTeamRequest("Engineering");
      Team team = org.mockito.Mockito.mock(Team.class);
      Team saved = org.mockito.Mockito.mock(Team.class);
      TeamResponse response =
          new TeamResponse(UUID.randomUUID(), "Engineering", false, null, null, null, null, null);

      when(teamRepository.existsByNameIgnoreCase("Engineering")).thenReturn(false);
      when(teamMapper.toEntity(request)).thenReturn(team);
      when(teamRepository.save(team)).thenReturn(saved);
      when(teamMapper.toTeamResponse(saved)).thenReturn(response);

      TeamResponse result = teamService.addTeam(request);

      assertThat(result).isEqualTo(response);
      verify(teamRepository).save(team);
    }

    @Test
    void addTeam_duplicateName_throwsResourceAlreadyExistsException() {
      CreateTeamRequest request = new CreateTeamRequest("Engineering");
      when(teamRepository.existsByNameIgnoreCase("Engineering")).thenReturn(true);

      assertThatThrownBy(() -> teamService.addTeam(request))
          .isInstanceOf(ResourceAlreadyExistsException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceAlreadyExistsException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_ALREADY_EXISTS));

      verify(teamRepository, never()).save(any(Team.class));
    }
  }

  @Nested
  class UpdateTeam {

    @Test
    void updateTeam_sameName_returnsWithoutSaving() {
      UUID teamId = UUID.randomUUID();
      Team team = org.mockito.Mockito.mock(Team.class);
      TeamResponse response =
          new TeamResponse(teamId, "Engineering", false, null, null, null, null, null);
      when(team.getName()).thenReturn("Engineering");
      when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
      when(teamMapper.toTeamResponse(team)).thenReturn(response);

      TeamResponse result = teamService.updateTeam(teamId, new UpdateTeamRequest("Engineering"));

      assertThat(result).isEqualTo(response);
      verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    void updateTeam_newName_updatesAndSaves() {
      UUID teamId = UUID.randomUUID();
      Team team = org.mockito.Mockito.mock(Team.class);
      Team saved = org.mockito.Mockito.mock(Team.class);
      TeamResponse response =
          new TeamResponse(teamId, "Platform", false, null, null, null, null, null);

      when(team.getName()).thenReturn("Engineering", "Engineering");
      when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
      when(teamRepository.existsByNameIgnoreCase("Platform")).thenReturn(false);
      when(teamRepository.save(team)).thenReturn(saved);
      when(teamMapper.toTeamResponse(saved)).thenReturn(response);

      TeamResponse result = teamService.updateTeam(teamId, new UpdateTeamRequest("Platform"));

      assertThat(result).isEqualTo(response);
      verify(team).setName("Platform");
      verify(teamRepository).save(team);
    }

    @Test
    void updateTeam_duplicateNewName_throwsResourceAlreadyExistsException() {
      UUID teamId = UUID.randomUUID();
      Team team = org.mockito.Mockito.mock(Team.class);

      when(team.getName()).thenReturn("Engineering", "Engineering");
      when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
      when(teamRepository.existsByNameIgnoreCase("Platform")).thenReturn(true);

      assertThatThrownBy(() -> teamService.updateTeam(teamId, new UpdateTeamRequest("Platform")))
          .isInstanceOf(ResourceAlreadyExistsException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceAlreadyExistsException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_ALREADY_EXISTS));

      verify(teamRepository, never()).save(any(Team.class));
    }
  }

  @Nested
  class GetTeamsByUserId {

    @Test
    void getTeamsByUserId_adminRole_returnsAllTeams() {
      Team team = org.mockito.Mockito.mock(Team.class);
      TeamResponse response =
          new TeamResponse(UUID.randomUUID(), "Engineering", false, null, null, null, null, null);
      when(teamRepository.findAll(any(org.springframework.data.domain.Sort.class)))
          .thenReturn(List.of(team));
      when(teamMapper.toTeamResponseWithoutUsers(team)).thenReturn(response);

      List<TeamResponse> result = teamService.getTeamsByUserId(UUID.randomUUID(), UserRole.ADMIN);

      assertThat(result).containsExactly(response);
      verify(teamRepository).findAll(any(org.springframework.data.domain.Sort.class));
    }

    @Test
    void getTeamsByUserId_nonAdminRole_returnsMembershipTeams() {
      UUID userId = UUID.randomUUID();
      Team team = org.mockito.Mockito.mock(Team.class);
      TeamResponse response =
          new TeamResponse(UUID.randomUUID(), "Engineering", false, null, null, null, null, null);
      when(teamRepository.findAllByUsers_Id(
              org.mockito.ArgumentMatchers.eq(userId),
              any(org.springframework.data.domain.Sort.class)))
          .thenReturn(List.of(team));
      when(teamMapper.toTeamResponseWithoutUsers(team)).thenReturn(response);

      List<TeamResponse> result = teamService.getTeamsByUserId(userId, UserRole.USER);

      assertThat(result).containsExactly(response);
      verify(teamRepository)
          .findAllByUsers_Id(
              org.mockito.ArgumentMatchers.eq(userId),
              any(org.springframework.data.domain.Sort.class));
    }
  }

  @Nested
  class GetUsersByTeamId {

    @Test
    void getUsersByTeamId_existingTeam_returnsMappedUsers() {
      UUID teamId = UUID.randomUUID();
      User user = org.mockito.Mockito.mock(User.class);
      UserBaseResponse userResponse =
          new UserBaseResponse(UUID.randomUUID(), "John", "Doe", "john@company.com");

      when(teamRepository.existsById(teamId)).thenReturn(true);
      when(userRepository.findUsersByTeamId(
              org.mockito.ArgumentMatchers.eq(teamId),
              any(org.springframework.data.domain.Sort.class)))
          .thenReturn(List.of(user));
      when(userMapper.toUserBaseResponse(user)).thenReturn(userResponse);

      List<UserBaseResponse> result = teamService.getUsersByTeamId(teamId);

      assertThat(result).containsExactly(userResponse);
      verify(userRepository)
          .findUsersByTeamId(
              org.mockito.ArgumentMatchers.eq(teamId),
              any(org.springframework.data.domain.Sort.class));
    }
  }

  @Nested
  class TeamLookup {

    @Test
    void getTeamById_missingId_throwsResourceNotFoundException() {
      UUID teamId = UUID.randomUUID();
      when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> teamService.getTeamById(teamId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void validateTeamExists_missingId_throwsResourceNotFoundException() {
      UUID teamId = UUID.randomUUID();
      when(teamRepository.existsById(teamId)).thenReturn(false);

      assertThatThrownBy(() -> teamService.validateTeamExists(teamId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void getUsersByTeamId_missingTeam_throwsResourceNotFoundException() {
      UUID teamId = UUID.randomUUID();
      when(teamRepository.existsById(teamId)).thenReturn(false);

      assertThatThrownBy(() -> teamService.getUsersByTeamId(teamId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }
  }

  @Nested
  class DeleteTeam {

    @Test
    void deleteTeam_existingTeam_deletesChildrenThenTeam() {
      UUID teamId = UUID.randomUUID();
      Team team = org.mockito.Mockito.mock(Team.class);

      when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

      teamService.deleteTeam(teamId);

      verify(metricValueRepository).deleteAllByTeamId(teamId);
      verify(metricRepository).deleteAllByTeamId(teamId);
      verify(rockRepository).deleteAllByTeamId(teamId);
      verify(todoRepository).deleteAllByTeamId(teamId);
      verify(issueRepository).deleteAllByTeamId(teamId);
      verify(headlineRepository).deleteAllByTeamId(teamId);
      verify(l10MeetingRepository).deleteAllByTeamId(teamId);
      verify(teamRepository).delete(team);
    }

    @Test
    void deleteTeam_nonExistentTeam_throwsResourceNotFoundException() {
      UUID teamId = UUID.randomUUID();
      when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> teamService.deleteTeam(teamId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));

      verify(metricValueRepository, never()).deleteAllByTeamId(any());
      verify(teamRepository, never()).delete(any());
    }
  }
}
