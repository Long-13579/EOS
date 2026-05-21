package com.ces.eos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ces.eos.dto.response.QuarterResponse;
import com.ces.eos.entity.Quarter;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.QuarterMapper;
import com.ces.eos.repository.QuarterRepository;
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
class QuarterServiceImplTest {

  @Mock private QuarterRepository quarterRepository;

  @Mock private QuarterMapper quarterMapper;

  @InjectMocks private QuarterServiceImpl quarterService;

  @Nested
  class GetQuarters {

    @Test
    void getQuarters_dataExists_returnsMappedList() {
      Quarter q1 = quarter(UUID.randomUUID(), "Q1");
      Quarter q2 = quarter(UUID.randomUUID(), "Q2");
      QuarterResponse r1 =
          new QuarterResponse(q1.getId(), "Q1", "Jan 1", "Mar 31", false, null, null, null, null);
      QuarterResponse r2 =
          new QuarterResponse(q2.getId(), "Q2", "Apr 1", "Jun 30", false, null, null, null, null);

      when(quarterRepository.findAllByOrderByNameAsc()).thenReturn(List.of(q1, q2));
      when(quarterMapper.toQuarterResponse(q1)).thenReturn(r1);
      when(quarterMapper.toQuarterResponse(q2)).thenReturn(r2);

      List<QuarterResponse> result = quarterService.getQuarters();

      assertThat(result).containsExactly(r1, r2);
      verify(quarterRepository).findAllByOrderByNameAsc();
    }

    @Test
    void getQuarters_noData_returnsEmptyList() {
      when(quarterRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

      List<QuarterResponse> result = quarterService.getQuarters();

      assertThat(result).isEmpty();
      verify(quarterRepository).findAllByOrderByNameAsc();
    }
  }

  @Nested
  class ValidateQuarterExists {

    @Test
    void validateQuarterExists_existingId_doesNotThrow() {
      UUID quarterId = UUID.randomUUID();
      when(quarterRepository.existsById(quarterId)).thenReturn(true);

      quarterService.validateQuarterExists(quarterId);

      verify(quarterRepository).existsById(quarterId);
    }

    @Test
    void validateQuarterExists_missingId_throwsResourceNotFoundException() {
      UUID quarterId = UUID.randomUUID();
      when(quarterRepository.existsById(quarterId)).thenReturn(false);

      assertThatThrownBy(() -> quarterService.validateQuarterExists(quarterId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));

      verify(quarterRepository).existsById(quarterId);
    }
  }

  @Nested
  class GetQuarterById {

    @Test
    void getQuarterById_existingId_returnsEntity() {
      UUID quarterId = UUID.randomUUID();
      Quarter quarter = quarter(quarterId, "Q1");
      when(quarterRepository.findById(quarterId)).thenReturn(Optional.of(quarter));

      Quarter result = quarterService.getQuarterById(quarterId);

      assertThat(result).isEqualTo(quarter);
      verify(quarterRepository).findById(quarterId);
    }

    @Test
    void getQuarterById_missingId_throwsResourceNotFoundException() {
      UUID quarterId = UUID.randomUUID();
      when(quarterRepository.findById(quarterId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> quarterService.getQuarterById(quarterId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));

      verify(quarterRepository).findById(quarterId);
    }
  }

  private static Quarter quarter(UUID id, String name) {
    return Quarter.builder()
        .id(id)
        .name(name)
        .startDate(MonthDay.of(1, 1))
        .endDate(MonthDay.of(3, 31))
        .build();
  }
}
