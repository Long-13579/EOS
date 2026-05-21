package com.ces.eos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ces.eos.dto.response.MetricResponse;
import com.ces.eos.entity.Metric;
import com.ces.eos.entity.MetricValue;
import com.ces.eos.entity.User;
import com.ces.eos.entity.Week;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.enums.MetricUnit;
import com.ces.eos.exception.BadRequestException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.exception.ServerInternalException;
import com.ces.eos.mapper.MetricMapper;
import com.ces.eos.repository.MetricRepository;
import com.ces.eos.repository.MetricValueRepository;
import com.ces.eos.service.UserService;
import com.ces.eos.service.WeekService;
import java.time.LocalDate;
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
class MetricValueServiceImplTest {

  @Mock private MetricValueRepository metricValueRepository;
  @Mock private MetricMapper metricMapper;
  @Mock private UserService userService;
  @Mock private WeekService weekService;
  @Mock private MetricRepository metricRepository;

  @InjectMocks private MetricValueServiceImpl metricValueService;

  @Nested
  class UpdateMetricValue {

    @Test
    void updateMetricValue_validInput_updatesAndReturnsMappedResponse() {
      UUID metricId = UUID.randomUUID();
      UUID updaterId = UUID.randomUUID();
      UUID currentWeekId = UUID.randomUUID();
      UUID previousWeekId = UUID.randomUUID();

      Metric metric = org.mockito.Mockito.mock(Metric.class);
      Week currentWeek = org.mockito.Mockito.mock(Week.class);
      Week previousWeek = org.mockito.Mockito.mock(Week.class);
      MetricValue currentValue = org.mockito.Mockito.mock(MetricValue.class);
      MetricValue previousValue = org.mockito.Mockito.mock(MetricValue.class);
      User updater = org.mockito.Mockito.mock(User.class);
      MetricResponse response =
          new MetricResponse(
              metricId,
              "M",
              "10",
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
      when(weekService.getOrCreateCurrentWeek()).thenReturn(currentWeek);
      when(currentWeek.getId()).thenReturn(currentWeekId);
      when(metricValueRepository.findByMetricIdAndWeekId(metricId, currentWeekId))
          .thenReturn(Optional.of(currentValue));
      when(userService.getUserById(updaterId)).thenReturn(updater);
      when(currentValue.getWeek()).thenReturn(currentWeek);
      when(currentWeek.getStartDate()).thenReturn(LocalDate.of(2026, 4, 14));
      when(metric.getId()).thenReturn(metricId);
      when(weekService.getPreviousWeek(LocalDate.of(2026, 4, 14)))
          .thenReturn(Optional.of(previousWeek));
      when(previousWeek.getId()).thenReturn(previousWeekId);
      when(metricValueRepository.findByMetricIdAndWeekId(metricId, previousWeekId))
          .thenReturn(Optional.of(previousValue));
      when(metricMapper.toMetricResponse(metric, currentValue, previousValue)).thenReturn(response);

      MetricResponse result = metricValueService.updateMetricValue(metricId, "12", updaterId);

      assertThat(result).isEqualTo(response);
      verify(currentValue).setValue("12");
      verify(currentValue).setUpdatedBy(updater);
      verify(metricValueRepository).save(currentValue);
    }

    @Test
    void updateMetricValue_metricMissing_throwsResourceNotFoundException() {
      UUID metricId = UUID.randomUUID();
      when(metricRepository.findByIdWithTeam(metricId)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> metricValueService.updateMetricValue(metricId, "12", UUID.randomUUID()))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void updateMetricValue_invalidValue_throwsBadRequestException() {
      UUID metricId = UUID.randomUUID();
      UUID weekId = UUID.randomUUID();
      Metric metric = org.mockito.Mockito.mock(Metric.class);
      Week week = org.mockito.Mockito.mock(Week.class);
      MetricValue currentValue = org.mockito.Mockito.mock(MetricValue.class);

      when(metricRepository.findByIdWithTeam(metricId)).thenReturn(Optional.of(metric));
      when(metric.getUnit()).thenReturn(MetricUnit.NUMBER);
      when(weekService.getOrCreateCurrentWeek()).thenReturn(week);
      when(week.getId()).thenReturn(weekId);
      when(metricValueRepository.findByMetricIdAndWeekId(metricId, weekId))
          .thenReturn(Optional.of(currentValue));
      when(userService.getUserById(any())).thenReturn(org.mockito.Mockito.mock(User.class));

      assertThatThrownBy(
              () -> metricValueService.updateMetricValue(metricId, "not-number", UUID.randomUUID()))
          .isInstanceOf(BadRequestException.class)
          .satisfies(
              ex ->
                  assertThat(((BadRequestException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.BAD_REQUEST));
    }

    @Test
    void updateMetricValue_insertedButStillMissing_throwsServerInternalException() {
      UUID metricId = UUID.randomUUID();
      UUID weekId = UUID.randomUUID();
      Metric metric = org.mockito.Mockito.mock(Metric.class);
      Week week = org.mockito.Mockito.mock(Week.class);

      when(metricRepository.findByIdWithTeam(metricId)).thenReturn(Optional.of(metric));
      when(weekService.getOrCreateCurrentWeek()).thenReturn(week);
      when(week.getId()).thenReturn(weekId);
      when(metricValueRepository.findByMetricIdAndWeekId(metricId, weekId))
          .thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> metricValueService.updateMetricValue(metricId, "12", UUID.randomUUID()))
          .isInstanceOf(ServerInternalException.class)
          .hasMessageContaining("Metric value not found");

      verify(metricValueRepository).insertIfNotExists(metricId, weekId);
    }
  }

  @Nested
  class FindByMetricIdsAndWeekIds {

    @Test
    void findByMetricIdsAndWeekIds_emptyInput_returnsEmptyList() {
      List<MetricValue> result =
          metricValueService.findByMetricIdsAndWeekIds(List.of(), List.of(UUID.randomUUID()));
      assertThat(result).isEmpty();
      verify(metricValueRepository, never()).findByMetricIdsAndWeekIds(any(), any());
    }

    @Test
    void findByMetricIdsAndWeekIds_validInput_queriesRepository() {
      UUID metricId = UUID.randomUUID();
      UUID weekId = UUID.randomUUID();
      MetricValue value = org.mockito.Mockito.mock(MetricValue.class);
      when(metricValueRepository.findByMetricIdsAndWeekIds(List.of(metricId), List.of(weekId)))
          .thenReturn(List.of(value));

      List<MetricValue> result =
          metricValueService.findByMetricIdsAndWeekIds(List.of(metricId), List.of(weekId));

      assertThat(result).containsExactly(value);
    }
  }

  @Nested
  class GroupByMetricIdAndWeekId {

    @Test
    void groupByMetricIdAndWeekId_emptyInput_returnsEmptyMap() {
      Map<UUID, Map<UUID, MetricValue>> result =
          metricValueService.groupByMetricIdAndWeekId(List.of());
      assertThat(result).isEmpty();
    }

    @Test
    void groupByMetricIdAndWeekId_validAndInvalidRows_groupsOnlyValidRows() {
      UUID metricId = UUID.randomUUID();
      UUID weekId = UUID.randomUUID();

      Metric metric = org.mockito.Mockito.mock(Metric.class);
      Week week = org.mockito.Mockito.mock(Week.class);
      MetricValue valid = org.mockito.Mockito.mock(MetricValue.class);
      MetricValue invalid = org.mockito.Mockito.mock(MetricValue.class);

      when(valid.getMetric()).thenReturn(metric);
      when(metric.getId()).thenReturn(metricId);
      when(valid.getWeek()).thenReturn(week);
      when(week.getId()).thenReturn(weekId);

      when(invalid.getMetric()).thenReturn(null);

      Map<UUID, Map<UUID, MetricValue>> result =
          metricValueService.groupByMetricIdAndWeekId(List.of(valid, invalid));

      assertThat(result).containsKey(metricId);
      assertThat(result.get(metricId)).containsEntry(weekId, valid);
    }
  }

  @Nested
  class AddDefaultMetricValueForWeek {

    @Test
    void addDefaultMetricValueForWeek_validInput_savesAndReturnsEntity() {
      Metric metric = org.mockito.Mockito.mock(Metric.class);
      Week week = org.mockito.Mockito.mock(Week.class);
      MetricValue saved = org.mockito.Mockito.mock(MetricValue.class);

      when(metricValueRepository.save(any(MetricValue.class))).thenReturn(saved);

      MetricValue result = metricValueService.addDefaultMetricValueForWeek(metric, week);

      assertThat(result).isEqualTo(saved);
      verify(metricValueRepository).save(any(MetricValue.class));
    }
  }
}
