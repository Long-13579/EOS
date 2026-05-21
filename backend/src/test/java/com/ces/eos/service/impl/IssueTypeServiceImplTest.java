package com.ces.eos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ces.eos.dto.response.IssueTypeBaseResponse;
import com.ces.eos.entity.IssueType;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.IssueTypeMapper;
import com.ces.eos.repository.IssueTypeRepository;
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
class IssueTypeServiceImplTest {

  @Mock private IssueTypeMapper issueTypeMapper;

  @Mock private IssueTypeRepository issueTypeRepository;

  @InjectMocks private IssueTypeServiceImpl issueTypeService;

  @Nested
  class GetIssueTypes {

    @Test
    void getIssueTypes_dataExists_returnsMappedList() {
      IssueType bug = new IssueType();
      IssueType task = new IssueType();
      IssueTypeBaseResponse bugResponse = new IssueTypeBaseResponse(UUID.randomUUID(), "Bug");
      IssueTypeBaseResponse taskResponse = new IssueTypeBaseResponse(UUID.randomUUID(), "Task");

      when(issueTypeRepository.findAllByOrderByNameAsc()).thenReturn(List.of(bug, task));
      when(issueTypeMapper.toIssueTypeBaseResponse(bug)).thenReturn(bugResponse);
      when(issueTypeMapper.toIssueTypeBaseResponse(task)).thenReturn(taskResponse);

      List<IssueTypeBaseResponse> result = issueTypeService.getIssueTypes();

      assertThat(result).containsExactly(bugResponse, taskResponse);
      verify(issueTypeRepository).findAllByOrderByNameAsc();
      verify(issueTypeMapper).toIssueTypeBaseResponse(bug);
      verify(issueTypeMapper).toIssueTypeBaseResponse(task);
    }

    @Test
    void getIssueTypes_noData_returnsEmptyList() {
      when(issueTypeRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

      List<IssueTypeBaseResponse> result = issueTypeService.getIssueTypes();

      assertThat(result).isEmpty();
      verify(issueTypeRepository).findAllByOrderByNameAsc();
    }
  }

  @Nested
  class GetIssueTypeById {

    @Test
    void getIssueTypeById_existingId_returnsEntity() {
      UUID issueTypeId = UUID.randomUUID();
      IssueType issueType = new IssueType();

      when(issueTypeRepository.findById(issueTypeId)).thenReturn(Optional.of(issueType));

      IssueType result = issueTypeService.getIssueTypeById(issueTypeId);

      assertThat(result).isEqualTo(issueType);
      verify(issueTypeRepository).findById(issueTypeId);
    }

    @Test
    void getIssueTypeById_missingId_throwsResourceNotFoundException() {
      UUID issueTypeId = UUID.randomUUID();
      when(issueTypeRepository.findById(issueTypeId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> issueTypeService.getIssueTypeById(issueTypeId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));

      verify(issueTypeRepository).findById(issueTypeId);
    }
  }

  @Nested
  class GetIssueTypeByName {

    @Test
    void getIssueTypeByName_existingName_returnsEntity() {
      IssueType issueType = new IssueType();
      when(issueTypeRepository.findByName("Long-term")).thenReturn(Optional.of(issueType));

      IssueType result = issueTypeService.getIssueTypeByName("Long-term");

      assertThat(result).isEqualTo(issueType);
      verify(issueTypeRepository).findByName("Long-term");
    }

    @Test
    void getIssueTypeByName_missingName_throwsIllegalStateException() {
      when(issueTypeRepository.findByName("Long-term")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> issueTypeService.getIssueTypeByName("Long-term"))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage(
              "Critical configuration error: Issue Type 'Long-term' not found in database.");

      verify(issueTypeRepository).findByName("Long-term");
    }
  }
}
