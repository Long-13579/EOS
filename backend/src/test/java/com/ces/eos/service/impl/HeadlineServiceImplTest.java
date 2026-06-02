package com.ces.eos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ces.eos.dto.request.CreateHeadlineRequest;
import com.ces.eos.dto.request.PaginationRequest;
import com.ces.eos.dto.request.UpdateHeadlineRequest;
import com.ces.eos.dto.response.HeadlineResponse;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.entity.Headline;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.User;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.exception.ConflictException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.HeadlineMapper;
import com.ces.eos.repository.HeadlineRepository;
import com.ces.eos.service.L10MeetingChangeLogService;
import com.ces.eos.service.TeamService;
import com.ces.eos.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class HeadlineServiceImplTest {

  @Mock private HeadlineRepository headlineRepository;
  @Mock private HeadlineMapper headlineMapper;
  @Mock private TeamService teamService;
  @Mock private UserService userService;
  @Mock private L10MeetingChangeLogService l10MeetingChangeLogService;
  @Mock private ObjectMapper objectMapper;

  @InjectMocks private HeadlineServiceImpl headlineService;

  @BeforeEach
  void setUp() {
    lenient().when(objectMapper.valueToTree(any())).thenReturn(mock(tools.jackson.databind.JsonNode.class));
  }

  @Nested
  class CreateHeadline {
    @Test
    void createHeadline_validRequest_returnsMappedResponse() {
      UUID creatorId = UUID.randomUUID();
      UUID teamId = UUID.randomUUID();
      CreateHeadlineRequest request = new CreateHeadlineRequest("Title", teamId);
      Headline headline = Headline.builder().title("Title").build();
      Team team = Team.builder().id(teamId).build();
      User creator = User.builder().id(creatorId).build();
      Headline saved = Headline.builder().id(UUID.randomUUID()).title("Title").build();
      HeadlineResponse response =
          new HeadlineResponse(saved.getId(), "Title", false, null, null, null, null, null);

      when(headlineMapper.toEntity(request)).thenReturn(headline);
      when(teamService.getTeamById(teamId)).thenReturn(team);
      when(userService.getUserById(creatorId)).thenReturn(creator);
      when(headlineRepository.save(headline)).thenReturn(saved);
      when(headlineMapper.toHeadlineResponse(saved)).thenReturn(response);

      HeadlineResponse result = headlineService.createHeadline(request, creatorId);

      assertThat(result).isEqualTo(response);
      assertThat(headline.getTeam()).isEqualTo(team);
      assertThat(headline.getCreatedBy()).isEqualTo(creator);
      assertThat(headline.getUpdatedBy()).isEqualTo(creator);
      verify(headlineRepository).save(headline);
    }
  }

  @Nested
  class GetHeadlinesByTeam {
    @Test
    void getHeadlinesByTeam_emptyIdsPage_returnsEmptyPagedResponse() {
      UUID teamId = UUID.randomUUID();
      PaginationRequest request = new PaginationRequest(1, 10);
      when(headlineRepository.findHeadlineIdsByTeamId(any(), any(), any()))
          .thenReturn(Page.empty(PageRequest.of(0, 10)));

      PagedEntityResponse<HeadlineResponse> result =
          headlineService.getHeadlinesByTeam(teamId, request, false);

      assertThat(result.data()).isEmpty();
      verify(teamService).validateTeamExists(teamId);
    }

    @Test
    void getHeadlinesByTeam_idsFound_returnsMappedItemsInOrder() {
      UUID teamId = UUID.randomUUID();
      UUID id1 = UUID.randomUUID();
      UUID id2 = UUID.randomUUID();
      PaginationRequest request = new PaginationRequest(1, 10);
      Page<UUID> idsPage = new PageImpl<>(List.of(id1, id2), PageRequest.of(0, 10), 2);
      Headline h1 = Headline.builder().id(id1).title("One").build();
      Headline h2 = Headline.builder().id(id2).title("Two").build();
      HeadlineResponse r1 = new HeadlineResponse(id1, "One", false, null, null, null, null, null);
      HeadlineResponse r2 = new HeadlineResponse(id2, "Two", false, null, null, null, null, null);

      when(headlineRepository.findHeadlineIdsByTeamId(any(), any(), any())).thenReturn(idsPage);
      when(headlineRepository.findAllByIdIn(List.of(id1, id2))).thenReturn(List.of(h1, h2));
      when(headlineMapper.toHeadlineResponse(h1)).thenReturn(r1);
      when(headlineMapper.toHeadlineResponse(h2)).thenReturn(r2);

      PagedEntityResponse<HeadlineResponse> result =
          headlineService.getHeadlinesByTeam(teamId, request, false);

      assertThat(result.data()).containsExactly(r1, r2);
    }
  }

  @Nested
  class UpdateHeadline {
    @Test
    void updateHeadline_archivedHeadline_throwsConflictException() {
      UUID headlineId = UUID.randomUUID();
      Headline archived = Headline.builder().id(headlineId).isArchived(true).build();
      when(headlineRepository.findById(headlineId)).thenReturn(Optional.of(archived));

      assertThatThrownBy(
              () ->
                  headlineService.updateHeadline(
                      headlineId, new UpdateHeadlineRequest("Updated"), UUID.randomUUID()))
          .isInstanceOf(ConflictException.class)
          .satisfies(
              ex ->
                  assertThat(((ConflictException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.CONFLICT));

      verify(headlineRepository, never()).save(any(Headline.class));
    }

    @Test
    void updateHeadline_missingHeadline_throwsResourceNotFoundException() {
      UUID headlineId = UUID.randomUUID();
      when(headlineRepository.findById(headlineId)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  headlineService.updateHeadline(
                      headlineId, new UpdateHeadlineRequest("Updated"), UUID.randomUUID()))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }
  }

  @Nested
  class ArchiveAndDelete {
    @Test
    void updateHeadlineArchiveStatus_sameStatus_returnsWithoutSaving() {
      UUID headlineId = UUID.randomUUID();
      Headline headline = Headline.builder().id(headlineId).isArchived(false).build();
      HeadlineResponse response =
          new HeadlineResponse(headlineId, "Title", false, null, null, null, null, null);
      when(headlineRepository.findById(headlineId)).thenReturn(Optional.of(headline));
      when(headlineMapper.toHeadlineResponse(headline)).thenReturn(response);

      HeadlineResponse result = headlineService.updateHeadlineArchiveStatus(headlineId, false);

      assertThat(result).isEqualTo(response);
      verify(headlineRepository, never()).save(any(Headline.class));
    }

    @Test
    void deleteHeadline_existingHeadline_deletesEntity() {
      UUID headlineId = UUID.randomUUID();
      Team team = Team.builder().id(UUID.randomUUID()).build();
      Headline headline = Headline.builder().id(headlineId).team(team).build();
      when(headlineRepository.findById(headlineId)).thenReturn(Optional.of(headline));

      headlineService.deleteHeadline(headlineId);

      verify(headlineRepository).delete(headline);
    }
  }
}
