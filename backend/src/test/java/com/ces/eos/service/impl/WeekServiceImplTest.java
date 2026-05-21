package com.ces.eos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ces.eos.dto.response.WeekResponse;
import com.ces.eos.entity.Week;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.WeekMapper;
import com.ces.eos.repository.WeekRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class WeekServiceImplTest {

  @Mock private WeekRepository weekRepository;

  @Mock private WeekMapper weekMapper;

  @InjectMocks private WeekServiceImpl weekService;

  @Nested
  class GetLast13Weeks {

    @Test
    void getLast13Weeks_weeksExist_returnsMappedList() {
      Week current = week();
      Week previous = week();
      WeekResponse currentResponse =
          new WeekResponse(UUID.randomUUID(), LocalDate.of(2026, 4, 13), LocalDate.of(2026, 4, 19));
      WeekResponse previousResponse =
          new WeekResponse(UUID.randomUUID(), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 12));

      when(weekRepository.findCurrentWeek()).thenReturn(Optional.of(current));
      when(weekRepository.findByStartDateLessThanEqual(any(LocalDate.class), any(Pageable.class)))
          .thenReturn(List.of(current, previous));
      when(weekMapper.toWeekResponse(current)).thenReturn(currentResponse);
      when(weekMapper.toWeekResponse(previous)).thenReturn(previousResponse);

      List<WeekResponse> result = weekService.getLast13Weeks();

      assertThat(result).containsExactly(currentResponse, previousResponse);
      verify(weekRepository).findCurrentWeek();
      verify(weekRepository)
          .findByStartDateLessThanEqual(any(LocalDate.class), any(Pageable.class));
    }

    @Test
    void getLast13Weeks_noWeeksAfterLookup_returnsEmptyList() {
      Week current = week();
      when(weekRepository.findCurrentWeek()).thenReturn(Optional.of(current));
      when(weekRepository.findByStartDateLessThanEqual(any(LocalDate.class), any(Pageable.class)))
          .thenReturn(List.of());

      List<WeekResponse> result = weekService.getLast13Weeks();

      assertThat(result).isEmpty();
      verify(weekRepository)
          .findByStartDateLessThanEqual(any(LocalDate.class), any(Pageable.class));
    }
  }

  @Nested
  class GetOrCreateCurrentWeek {

    @Test
    void getOrCreateCurrentWeek_existingWeek_returnsWeek() {
      Week current = week();
      when(weekRepository.findCurrentWeek()).thenReturn(Optional.of(current));

      Week result = weekService.getOrCreateCurrentWeek();

      assertThat(result).isEqualTo(current);
      verify(weekRepository).findCurrentWeek();
    }

    @Test
    void getOrCreateCurrentWeek_missingWeekAndCreated_returnsCreatedWeek() {
      Week created = week();
      when(weekRepository.findCurrentWeek())
          .thenReturn(Optional.empty())
          .thenReturn(Optional.of(created));

      Week result = weekService.getOrCreateCurrentWeek();

      assertThat(result).isEqualTo(created);
      verify(weekRepository).insertWeekIfNotExists(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getOrCreateCurrentWeek_missingAfterCreate_throwsIllegalStateException() {
      when(weekRepository.findCurrentWeek())
          .thenReturn(Optional.empty())
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> weekService.getOrCreateCurrentWeek())
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Failed to create or retrieve current week");

      verify(weekRepository).insertWeekIfNotExists(any(LocalDate.class), any(LocalDate.class));
    }
  }

  @Nested
  class GetPreviousWeek {

    @Test
    void getPreviousWeek_exists_returnsOptionalWithWeek() {
      LocalDate startDate = LocalDate.of(2026, 4, 13);
      Week previous = week();
      when(weekRepository.findTopByStartDateLessThanOrderByStartDateDesc(startDate))
          .thenReturn(Optional.of(previous));

      Optional<Week> result = weekService.getPreviousWeek(startDate);

      assertThat(result).contains(previous);
      verify(weekRepository).findTopByStartDateLessThanOrderByStartDateDesc(startDate);
    }

    @Test
    void getPreviousWeek_notExists_returnsEmptyOptional() {
      LocalDate startDate = LocalDate.of(2026, 4, 13);
      when(weekRepository.findTopByStartDateLessThanOrderByStartDateDesc(startDate))
          .thenReturn(Optional.empty());

      Optional<Week> result = weekService.getPreviousWeek(startDate);

      assertThat(result).isEmpty();
      verify(weekRepository).findTopByStartDateLessThanOrderByStartDateDesc(startDate);
    }
  }

  @Nested
  class GetWeekById {

    @Test
    void getWeekById_existingId_returnsWeek() {
      UUID weekId = UUID.randomUUID();
      Week week = week();
      when(weekRepository.findById(weekId)).thenReturn(Optional.of(week));

      Week result = weekService.getWeekById(weekId);

      assertThat(result).isEqualTo(week);
      verify(weekRepository).findById(weekId);
    }

    @Test
    void getWeekById_missingId_throwsResourceNotFoundException() {
      UUID weekId = UUID.randomUUID();
      when(weekRepository.findById(weekId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> weekService.getWeekById(weekId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));

      verify(weekRepository).findById(weekId);
    }
  }

  private static Week week() {
    return mock(Week.class);
  }
}
