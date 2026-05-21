package com.ces.eos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ces.eos.dto.request.CreateMetricRequest;
import com.ces.eos.dto.request.UpdateMetricRequest;
import com.ces.eos.dto.response.MetricResponse;
import com.ces.eos.dto.response.TrendsTabMetricListResponse;
import com.ces.eos.entity.Metric;
import com.ces.eos.entity.MetricValue;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.User;
import com.ces.eos.entity.Week;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.enums.MetricOperator;
import com.ces.eos.enums.MetricUnit;
import com.ces.eos.exception.BadRequestException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.MetricMapper;
import com.ces.eos.mapper.MetricValueMapper;
import com.ces.eos.repository.MetricRepository;
import com.ces.eos.service.MetricValueService;
import com.ces.eos.service.TeamService;
import com.ces.eos.service.UserService;
import com.ces.eos.service.WeekService;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricServiceImplTest {

  @Mock private MetricRepository metricRepository;
  @Mock private MetricValueService metricValueService;
  @Mock private MetricMapper metricMapper;
  @Mock private MetricValueMapper metricValueMapper;
  @Mock private WeekService weekService;
  @Mock private UserService userService;
  @Mock private TeamService teamService;

  @InjectMocks private MetricServiceImpl metricService;

  @Nested
  class AddMetric {

    @Test
    void addMetric_validRequest_returnsMappedResponse() {
      UUID teamId = UUID.randomUUID();
      UUID ownerId = UUID.randomUUID();
      UUID creatorId = UUID.randomUUID();
      CreateMetricRequest request =
          new CreateMetricRequest("Revenue", "100", "NUMBER", "GREATER_THAN", teamId, ownerId);

      Metric metric = org.mockito.Mockito.mock(Metric.class);
      Metric savedMetric = org.mockito.Mockito.mock(Metric.class);
      MetricValue defaultValue = org.mockito.Mockito.mock(MetricValue.class);
      User owner = org.mockito.Mockito.mock(User.class);
      User creator = org.mockito.Mockito.mock(User.class);
      Team team = org.mockito.Mockito.mock(Team.class);
      Week currentWeek = org.mockito.Mockito.mock(Week.class);
      MetricResponse response =
          new MetricResponse(
              UUID.randomUUID(),
              "Revenue",
              "100",
              "NUMBER",
              "GREATER_THAN",
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null);

      when(metricMapper.toEntity(request)).thenReturn(metric);
      when(metric.getUnit()).thenReturn(MetricUnit.NUMBER);
      when(metric.getOperator()).thenReturn(MetricOperator.GREATER_THAN);
      when(metric.getGoal()).thenReturn("100");
      when(userService.getUserByIdAndTeamId(ownerId, teamId)).thenReturn(owner);
      when(userService.getUserById(creatorId)).thenReturn(creator);
      when(teamService.getTeamById(teamId)).thenReturn(team);
      when(metricRepository.save(metric)).thenReturn(savedMetric);
      when(weekService.getOrCreateCurrentWeek()).thenReturn(currentWeek);
      when(metricValueService.addDefaultMetricValueForWeek(savedMetric, currentWeek))
          .thenReturn(defaultValue);
      when(metricMapper.toMetricResponse(savedMetric, defaultValue, null)).thenReturn(response);

      MetricResponse result = metricService.addMetric(request, creatorId);

      assertThat(result).isEqualTo(response);
      verify(metric).setOwner(owner);
      verify(metric).setCreatedBy(creator);
      verify(metric).setUpdatedBy(creator);
      verify(metric).setTeam(team);
      verify(metricRepository).save(metric);
    }

    @Test
    void addMetric_invalidMetric_throwsBadRequestException() {
      CreateMetricRequest request =
          new CreateMetricRequest(
              "Revenue", "", "NUMBER", "GREATER_THAN", UUID.randomUUID(), UUID.randomUUID());
      Metric metric = org.mockito.Mockito.mock(Metric.class);

      when(metricMapper.toEntity(request)).thenReturn(metric);
      when(metric.getUnit()).thenReturn(MetricUnit.NUMBER);
      when(metric.getOperator()).thenReturn(MetricOperator.GREATER_THAN);
      when(metric.getGoal()).thenReturn("");

      assertThatThrownBy(() -> metricService.addMetric(request, UUID.randomUUID()))
          .isInstanceOf(BadRequestException.class)
          .satisfies(
              ex ->
                  assertThat(((BadRequestException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.BAD_REQUEST));

      verify(metricRepository, never()).save(any(Metric.class));
    }
  }

  @Nested
  class ListMetrics {

    @Test
    void listMetricsByTeamAndWeek_noMetrics_returnsEmptyList() {
      UUID teamId = UUID.randomUUID();
      UUID weekId = UUID.randomUUID();
      when(metricRepository.findByTeamId(teamId)).thenReturn(List.of());

      List<MetricResponse> result = metricService.listMetricsByTeamAndWeek(teamId, weekId);

      assertThat(result).isEmpty();
      verify(weekService, never()).getWeekById(any());
    }

    @Test
    void listMetricsByTeamAndWeek_metricsExist_returnsMappedList() {
      UUID teamId = UUID.randomUUID();
      UUID weekId = UUID.randomUUID();
      UUID metricId = UUID.randomUUID();

      Metric metric = org.mockito.Mockito.mock(Metric.class);
      Week currentWeek = org.mockito.Mockito.mock(Week.class);
      MetricValue currentValue = org.mockito.Mockito.mock(MetricValue.class);
      MetricResponse response =
          new MetricResponse(
              metricId,
              "Revenue",
              "100",
              "NUMBER",
              "GREATER_THAN",
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null);

      when(metricRepository.findByTeamId(teamId)).thenReturn(List.of(metric));
      when(weekService.getWeekById(weekId)).thenReturn(currentWeek);
      when(currentWeek.getStartDate()).thenReturn(LocalDate.of(2026, 4, 14));
      when(weekService.getPreviousWeek(LocalDate.of(2026, 4, 14))).thenReturn(Optional.empty());
      when(metric.getId()).thenReturn(metricId);
      when(metricValueService.findByMetricIdsAndWeekIds(List.of(metricId), List.of(weekId)))
          .thenReturn(List.of(currentValue));
      Map<UUID, MetricValue> weekValues = new HashMap<>();
      weekValues.put(weekId, currentValue);
      when(metricValueService.groupByMetricIdAndWeekId(List.of(currentValue)))
          .thenReturn(Map.of(metricId, weekValues));
      when(metricMapper.toMetricResponse(metric, currentValue, null)).thenReturn(response);

      List<MetricResponse> result = metricService.listMetricsByTeamAndWeek(teamId, weekId);

      assertThat(result).containsExactly(response);
    }

    @Test
    void listMetricsByTeamAndWeek_previousWeekPresent_includesTrendValue() {
      UUID teamId = UUID.randomUUID();
      UUID weekId = UUID.randomUUID();
      UUID previousWeekId = UUID.randomUUID();
      UUID metricId = UUID.randomUUID();

      Metric metric = org.mockito.Mockito.mock(Metric.class);
      Week currentWeek = org.mockito.Mockito.mock(Week.class);
      Week previousWeek = org.mockito.Mockito.mock(Week.class);
      MetricValue currentValue = org.mockito.Mockito.mock(MetricValue.class);
      MetricValue previousValue = org.mockito.Mockito.mock(MetricValue.class);
      MetricResponse response =
          new MetricResponse(
              metricId,
              "Revenue",
              "100",
              "NUMBER",
              "GREATER_THAN",
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null);

      when(metricRepository.findByTeamId(teamId)).thenReturn(List.of(metric));
      when(weekService.getWeekById(weekId)).thenReturn(currentWeek);
      when(currentWeek.getStartDate()).thenReturn(LocalDate.of(2026, 4, 14));
      when(weekService.getPreviousWeek(LocalDate.of(2026, 4, 14))).thenReturn(Optional.of(previousWeek));
      when(previousWeek.getId()).thenReturn(previousWeekId);
      when(metric.getId()).thenReturn(metricId);
      when(metricValueService.findByMetricIdsAndWeekIds(List.of(metricId), List.of(weekId, previousWeekId)))
          .thenReturn(List.of(currentValue, previousValue));

      Map<UUID, MetricValue> weekValues = new HashMap<>();
      weekValues.put(weekId, currentValue);
      weekValues.put(previousWeekId, previousValue);
      when(metricValueService.groupByMetricIdAndWeekId(List.of(currentValue, previousValue)))
          .thenReturn(Map.of(metricId, weekValues));
      when(metricMapper.toMetricResponse(metric, currentValue, previousValue)).thenReturn(response);

      List<MetricResponse> result = metricService.listMetricsByTeamAndWeek(teamId, weekId);

      assertThat(result).containsExactly(response);
      verify(metricMapper).toMetricResponse(metric, currentValue, previousValue);
    }
  }

  @Nested
  class TrendsTab {

    @Test
    void listTrendsTabMetricsByTeam_noMetrics_returnsEmptyItems() {
      UUID teamId = UUID.randomUUID();
      when(metricRepository.findByTeamId(teamId)).thenReturn(List.of());

      TrendsTabMetricListResponse result = metricService.listTrendsTabMetricsByTeam(teamId);

      assertThat(result.items()).isEmpty();
    }

    @Test
    void listTrendsTabMetricsByTeam_noWeeks_returnsEmptyItems() {
      UUID teamId = UUID.randomUUID();
      Metric metric = org.mockito.Mockito.mock(Metric.class);

      when(metricRepository.findByTeamId(teamId)).thenReturn(List.of(metric));
      when(weekService.getLast13Weeks()).thenReturn(List.of());

      TrendsTabMetricListResponse result = metricService.listTrendsTabMetricsByTeam(teamId);

      assertThat(result.items()).isEmpty();
      verify(metricValueService, never()).findByMetricIdsAndWeekIds(any(), any());
    }
  }

  @Nested
  class UpdateMetric {

    @Test
    void updateMetric_validRequest_updatesAndReturnsResponse() {
      UUID metricId = UUID.randomUUID();
      UUID teamId = UUID.randomUUID();
      UUID ownerId = UUID.randomUUID();
      UUID updaterId = UUID.randomUUID();

      UpdateMetricRequest request =
          new UpdateMetricRequest("Revenue", "120", "GREATER_THAN", ownerId);

      Metric metric = org.mockito.Mockito.mock(Metric.class);
      Team team = org.mockito.Mockito.mock(Team.class);
      User owner = org.mockito.Mockito.mock(User.class);
      User updater = org.mockito.Mockito.mock(User.class);
      Metric updatedMetric = org.mockito.Mockito.mock(Metric.class);
      MetricResponse response =
          new MetricResponse(
              metricId,
              "Revenue",
              "120",
              "NUMBER",
              "GREATER_THAN",
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null);

      when(metricRepository.findByIdWithTeam(metricId)).thenReturn(Optional.of(metric));
      when(metric.getUnit()).thenReturn(MetricUnit.NUMBER);
      when(metric.getTeam()).thenReturn(team);
      when(team.getId()).thenReturn(teamId);
      when(userService.getUserByIdAndTeamId(ownerId, teamId)).thenReturn(owner);
      when(userService.getUserById(updaterId)).thenReturn(updater);
      when(metricRepository.save(metric)).thenReturn(updatedMetric);
      when(metricMapper.toMetricResponse(updatedMetric, null, null)).thenReturn(response);

      MetricResponse result = metricService.updateMetric(metricId, request, updaterId);

      assertThat(result).isEqualTo(response);
      verify(metric).setName("Revenue");
      verify(metric).setGoal("120");
      verify(metric).setOperator(MetricOperator.GREATER_THAN);
      verify(metric).setOwner(owner);
      verify(metric).setUpdatedBy(updater);
      verify(metricRepository).save(metric);
    }

    @Test
    void updateMetric_invalidOperator_throwsBadRequestException() {
      UUID metricId = UUID.randomUUID();
      Metric metric = org.mockito.Mockito.mock(Metric.class);
      when(metricRepository.findByIdWithTeam(metricId)).thenReturn(Optional.of(metric));

      UpdateMetricRequest request =
          new UpdateMetricRequest("Revenue", "120", "INVALID", UUID.randomUUID());

      assertThatThrownBy(() -> metricService.updateMetric(metricId, request, UUID.randomUUID()))
          .isInstanceOf(BadRequestException.class)
          .satisfies(
              ex ->
                  assertThat(((BadRequestException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.BAD_REQUEST));
    }

    @Test
    void updateMetric_missingMetric_throwsResourceNotFoundException() {
      UUID metricId = UUID.randomUUID();
      when(metricRepository.findByIdWithTeam(metricId)).thenReturn(Optional.empty());

      UpdateMetricRequest request =
          new UpdateMetricRequest("Revenue", "120", "GREATER_THAN", UUID.randomUUID());

      assertThatThrownBy(() -> metricService.updateMetric(metricId, request, UUID.randomUUID()))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }
  }

  @Nested
  class GetMetricById {

    @Test
    void getMetricById_missingMetric_throwsResourceNotFoundException() {
      UUID metricId = UUID.randomUUID();
      when(metricRepository.findByIdWithTeam(metricId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> metricService.getMetricById(metricId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }
  }
}
