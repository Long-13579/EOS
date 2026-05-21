package com.ces.eos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ces.eos.dto.response.YearResponse;
import com.ces.eos.entity.CustomYear;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.exception.ServerInternalException;
import com.ces.eos.mapper.YearMapper;
import com.ces.eos.repository.YearRepository;
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
class YearServiceImplTest {

  @Mock private YearRepository yearRepository;

  @Mock private YearMapper yearMapper;

  @InjectMocks private YearServiceImpl yearService;

  @Nested
  class GetYears {

    @Test
    void getYears_dataExists_returnsMappedList() {
      CustomYear y2024 = year();
      CustomYear y2025 = year();
      YearResponse r2024 = new YearResponse(UUID.randomUUID(), 2024, false, null);
      YearResponse r2025 = new YearResponse(UUID.randomUUID(), 2025, false, null);

      when(yearRepository.findAllByOrderByYearAsc()).thenReturn(List.of(y2024, y2025));
      when(yearMapper.toYearResponse(y2024)).thenReturn(r2024);
      when(yearMapper.toYearResponse(y2025)).thenReturn(r2025);

      List<YearResponse> result = yearService.getYears();

      assertThat(result).containsExactly(r2024, r2025);
      verify(yearRepository).findAllByOrderByYearAsc();
    }

    @Test
    void getYears_noData_returnsEmptyList() {
      when(yearRepository.findAllByOrderByYearAsc()).thenReturn(List.of());

      List<YearResponse> result = yearService.getYears();

      assertThat(result).isEmpty();
      verify(yearRepository).findAllByOrderByYearAsc();
    }
  }

  @Nested
  class ValidateYearExists {

    @Test
    void validateYearExists_existingId_doesNotThrow() {
      UUID yearId = UUID.randomUUID();
      when(yearRepository.existsById(yearId)).thenReturn(true);

      yearService.validateYearExists(yearId);

      verify(yearRepository).existsById(yearId);
    }

    @Test
    void validateYearExists_missingId_throwsResourceNotFoundException() {
      UUID yearId = UUID.randomUUID();
      when(yearRepository.existsById(yearId)).thenReturn(false);

      assertThatThrownBy(() -> yearService.validateYearExists(yearId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));

      verify(yearRepository).existsById(yearId);
    }
  }

  @Nested
  class GetOrCreateYear {

    @Test
    void getOrCreateYear_rowExistsAfterUpsert_returnsYear() {
      CustomYear year = year();
      when(yearRepository.findByYear(2026)).thenReturn(Optional.of(year));

      CustomYear result = yearService.getOrCreateYear(2026);

      assertThat(result).isEqualTo(year);
      verify(yearRepository).insertIfNotExists(2026);
      verify(yearRepository).findByYear(2026);
    }

    @Test
    void getOrCreateYear_missingAfterUpsert_throwsServerInternalException() {
      when(yearRepository.findByYear(2026)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> yearService.getOrCreateYear(2026))
          .isInstanceOf(ServerInternalException.class)
          .hasMessageContaining("Year upsert produced no row for year 2026");

      verify(yearRepository).insertIfNotExists(2026);
      verify(yearRepository).findByYear(2026);
    }
  }

  private static CustomYear year() {
    return mock(CustomYear.class);
  }
}
