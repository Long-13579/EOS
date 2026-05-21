package com.ces.eos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.enums.RockCategory;
import com.ces.eos.enums.RockStatus;
import com.ces.eos.exception.BadRequestException;
import com.ces.eos.exception.ConflictException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.RockMapper;
import com.ces.eos.repository.RockRepository;
import com.ces.eos.service.QuarterService;
import com.ces.eos.service.TeamService;
import com.ces.eos.service.UserService;
import com.ces.eos.service.YearService;
import java.time.Instant;
import java.time.MonthDay;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RockServiceImplTest {

  @Mock private RockRepository rockRepository;
  @Mock private RockMapper rockMapper;
  @Mock private YearService yearService;
  @Mock private QuarterService quarterService;
  @Mock private TeamService teamService;
  @Mock private UserService userService;

  @InjectMocks private RockServiceImpl rockService;

  @Nested
  class GetRocksByTeam {

    @Test
    void getRocksByTeam_leadershipTeam_returnsGroupedResponse() {
      UUID teamId = UUID.randomUUID();
      UUID yearId = UUID.randomUUID();
      UUID quarterId = UUID.randomUUID();

      Team leadership = Team.builder().id(teamId).isLeadership(true).build();
      Rock companyRock =
          Rock.builder().id(UUID.randomUUID()).category(RockCategory.COMPANY).build();
      RockResponse companyResponse =
          new RockResponse(
              companyRock.getId(),
              "C",
              "D",
              RockStatus.ON_TRACK,
              RockCategory.COMPANY,
              false,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null);

      when(teamService.getTeamById(teamId)).thenReturn(leadership);
      when(rockRepository.findAllByTeamIdAndYearIdAndQuarterId(teamId, yearId, quarterId, false))
          .thenReturn(List.of(companyRock));
      when(rockMapper.toRockResponse(companyRock)).thenReturn(companyResponse);

      RockListResponse result = rockService.getRocksByTeam(teamId, yearId, quarterId, false);

      assertThat(result.companyRocks()).containsExactly(companyResponse);
      verify(yearService).validateYearExists(yearId);
      verify(quarterService).validateQuarterExists(quarterId);
    }

    @Test
    void getRocksByTeam_nonLeadershipTeam_hidesCompanyRocks() {
      UUID teamId = UUID.randomUUID();
      UUID yearId = UUID.randomUUID();
      UUID quarterId = UUID.randomUUID();

      Team normal = Team.builder().id(teamId).isLeadership(false).build();
      Rock companyRock =
          Rock.builder().id(UUID.randomUUID()).category(RockCategory.COMPANY).build();

      when(teamService.getTeamById(teamId)).thenReturn(normal);
      when(rockRepository.findAllByTeamIdAndYearIdAndQuarterId(teamId, yearId, quarterId, false))
          .thenReturn(List.of(companyRock));

      RockListResponse result = rockService.getRocksByTeam(teamId, yearId, quarterId, false);

      assertThat(result.companyRocks()).isNull();
      assertThat(result.departmentRocks()).isEmpty();
      assertThat(result.individualRocks()).isEmpty();
    }
  }

  @Nested
  class AddRock {

    @Test
    void addRock_companyRockOnNonLeadership_throwsBadRequestException() {
      UUID teamId = UUID.randomUUID();
      CreateRockRequest request =
          new CreateRockRequest(
              "Rock",
              "Desc",
              "ON_TRACK",
              "COMPANY",
              Instant.parse("2026-02-01T00:00:00Z"),
              teamId,
              2026,
              UUID.randomUUID(),
              UUID.randomUUID());

      Rock rock = org.mockito.Mockito.mock(Rock.class);
      Team team = Team.builder().id(teamId).isLeadership(false).build();

      when(rockMapper.toEntity(request)).thenReturn(rock);
      when(teamService.getTeamById(teamId)).thenReturn(team);
      when(rock.getCategory()).thenReturn(RockCategory.COMPANY);

      assertThatThrownBy(() -> rockService.addRock(request, UUID.randomUUID()))
          .isInstanceOf(BadRequestException.class)
          .satisfies(
              ex ->
                  assertThat(((BadRequestException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.BAD_REQUEST));

      verify(rockRepository, never()).save(any(Rock.class));
    }
  }

  @Nested
  class UpdateRock {

    @Test
    void updateRock_archivedRock_throwsConflictException() {
      UUID rockId = UUID.randomUUID();
      Rock rock = org.mockito.Mockito.mock(Rock.class);
      Team team = Team.builder().id(UUID.randomUUID()).isLeadership(true).build();

      when(rockRepository.findById(rockId)).thenReturn(Optional.of(rock));
      when(rock.getTeam()).thenReturn(team);
      when(rock.getCategory()).thenReturn(RockCategory.INDIVIDUAL);
      when(rock.getIsArchived()).thenReturn(true);

      UpdateRockRequest request =
          new UpdateRockRequest(
              "Rock",
              "Desc",
              "ON_TRACK",
              "INDIVIDUAL",
              Instant.parse("2026-02-01T00:00:00Z"),
              2026,
              UUID.randomUUID(),
              UUID.randomUUID());

      assertThatThrownBy(() -> rockService.updateRock(rockId, request, UUID.randomUUID()))
          .isInstanceOf(ConflictException.class)
          .satisfies(
              ex ->
                  assertThat(((ConflictException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.CONFLICT));

      verify(rockRepository, never()).save(any(Rock.class));
    }
  }

  @Nested
  class ArchiveAndStatus {

    @Test
    void updateRockArchiveStatus_sameStatus_returnsWithoutSave() {
      UUID rockId = UUID.randomUUID();
      Team team = Team.builder().id(UUID.randomUUID()).isLeadership(true).build();
      Rock rock = org.mockito.Mockito.mock(Rock.class);
      RockResponse response =
          new RockResponse(
              rockId,
              "Rock",
              "Desc",
              RockStatus.ON_TRACK,
              RockCategory.INDIVIDUAL,
              false,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null);

      when(rockRepository.findById(rockId)).thenReturn(Optional.of(rock));
      when(rock.getTeam()).thenReturn(team);
      when(rock.getCategory()).thenReturn(RockCategory.INDIVIDUAL);
      when(rock.getIsArchived()).thenReturn(false);
      when(rockMapper.toRockResponse(rock)).thenReturn(response);

      RockResponse result = rockService.updateRockArchiveStatus(rockId, false);

      assertThat(result).isEqualTo(response);
      verify(rockRepository, never()).save(any(Rock.class));
    }

    @Test
    void updateRockStatus_sameStatus_returnsWithoutSave() {
      UUID rockId = UUID.randomUUID();
      Team team = Team.builder().id(UUID.randomUUID()).isLeadership(true).build();
      Rock rock = org.mockito.Mockito.mock(Rock.class);
      RockResponse response =
          new RockResponse(
              rockId,
              "Rock",
              "Desc",
              RockStatus.ON_TRACK,
              RockCategory.INDIVIDUAL,
              false,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null);

      when(rockRepository.findById(rockId)).thenReturn(Optional.of(rock));
      when(rock.getTeam()).thenReturn(team);
      when(rock.getCategory()).thenReturn(RockCategory.INDIVIDUAL);
      when(rock.getStatus()).thenReturn(RockStatus.ON_TRACK);
      when(rockMapper.toRockResponse(rock)).thenReturn(response);

      RockResponse result = rockService.updateRockStatus(rockId, RockStatus.ON_TRACK);

      assertThat(result).isEqualTo(response);
      verify(rockRepository, never()).save(any(Rock.class));
    }
  }

  @Nested
  class FindActiveRocksByOwner {

    @Test
    void findActiveRocksByOwnerId_validRequest_returnsMappedResponse() {
      UUID ownerId = UUID.randomUUID();
      UUID yearId = UUID.randomUUID();
      UUID quarterId = UUID.randomUUID();
      Rock rock = Rock.builder().id(UUID.randomUUID()).category(RockCategory.INDIVIDUAL).build();
      RockResponse rockResponse =
          new RockResponse(
              rock.getId(),
              "Rock",
              "Desc",
              RockStatus.ON_TRACK,
              RockCategory.INDIVIDUAL,
              false,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null);

      when(rockRepository.findActiveRocksByOwnerIdAndYearAndQuarter(ownerId, yearId, quarterId))
          .thenReturn(List.of(rock));
      when(rockMapper.toRockResponse(rock)).thenReturn(rockResponse);

      UserRockListResponse result =
          rockService.findActiveRocksByOwnerId(ownerId, yearId, quarterId);

      assertThat(result.data()).containsExactly(rockResponse);
      verify(yearService).validateYearExists(yearId);
      verify(quarterService).validateQuarterExists(quarterId);
    }

    @Test
    void getRockById_missingRock_throwsResourceNotFoundException() {
      UUID rockId = UUID.randomUUID();
      when(rockRepository.findById(rockId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> rockService.getRockById(rockId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }
  }

  @Nested
  class DueDateValidation {

    @Test
    void addRock_dueDateOutOfQuarter_throwsBadRequestException() {
      UUID teamId = UUID.randomUUID();
      UUID quarterId = UUID.randomUUID();
      UUID ownerId = UUID.randomUUID();

      CreateRockRequest request =
          new CreateRockRequest(
              "Rock",
              "Desc",
              "ON_TRACK",
              "INDIVIDUAL",
              Instant.parse("2026-08-01T00:00:00Z"),
              teamId,
              2026,
              quarterId,
              ownerId);

      Rock rock = org.mockito.Mockito.mock(Rock.class);
      Team team = Team.builder().id(teamId).isLeadership(true).build();
      Quarter quarter =
          Quarter.builder()
              .id(quarterId)
              .name("Q1")
              .startDate(MonthDay.of(1, 1))
              .endDate(MonthDay.of(3, 31))
              .build();
      CustomYear year = CustomYear.builder().id(UUID.randomUUID()).year(2026).build();
      User creator = org.mockito.Mockito.mock(User.class);

      when(rockMapper.toEntity(request)).thenReturn(rock);
      when(rock.getCategory()).thenReturn(RockCategory.INDIVIDUAL);
      when(teamService.getTeamById(teamId)).thenReturn(team);
      when(userService.getUserById(any())).thenReturn(creator);
      when(quarterService.getQuarterById(quarterId)).thenReturn(quarter);
      when(yearService.getOrCreateYear(2026)).thenReturn(year);

      assertThatThrownBy(() -> rockService.addRock(request, UUID.randomUUID()))
          .isInstanceOf(BadRequestException.class)
          .satisfies(
              ex ->
                  assertThat(((BadRequestException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.BAD_REQUEST));

      verify(rockRepository, never()).save(any(Rock.class));
    }
  }
}
