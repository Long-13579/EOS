package com.ces.eos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ces.eos.dto.request.CreateIssueRequest;
import com.ces.eos.dto.request.PaginationRequest;
import com.ces.eos.dto.request.UpdateIssueRequest;
import com.ces.eos.dto.response.IssueResponse;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.entity.Issue;
import com.ces.eos.entity.IssueType;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.User;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.exception.BadRequestException;
import com.ces.eos.exception.ConflictException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.IssueMapper;
import com.ces.eos.repository.IssueRepository;
import com.ces.eos.service.IssueTypeService;
import com.ces.eos.service.TeamService;
import com.ces.eos.service.UserService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class IssueServiceImplTest {

  @Mock private IssueRepository issueRepository;
  @Mock private IssueMapper issueMapper;
  @Mock private TeamService teamService;
  @Mock private UserService userService;
  @Mock private IssueTypeService issueTypeService;

  @InjectMocks private IssueServiceImpl issueService;

  @Nested
  class AddIssue {
    @Test
    void addIssue_withIssueType_setsDependenciesAndReturnsResponse() {
      UUID creatorId = UUID.randomUUID();
      UUID teamId = UUID.randomUUID();
      UUID issueTypeId = UUID.randomUUID();
      CreateIssueRequest request = new CreateIssueRequest("Bug", "desc", issueTypeId, teamId);
      Issue issue = org.mockito.Mockito.mock(Issue.class);
      IssueType issueType = org.mockito.Mockito.mock(IssueType.class);
      Team team = org.mockito.Mockito.mock(Team.class);
      User creator = org.mockito.Mockito.mock(User.class);
      Issue saved = org.mockito.Mockito.mock(Issue.class);
      IssueResponse response =
          new IssueResponse(
              UUID.randomUUID(), "Bug", "desc", null, false, null, null, null, null, null, null);

      when(issueMapper.toEntity(request)).thenReturn(issue);
      when(issueTypeService.getIssueTypeById(issueTypeId)).thenReturn(issueType);
      when(userService.getUserById(creatorId)).thenReturn(creator);
      when(teamService.getTeamById(teamId)).thenReturn(team);
      when(issueRepository.save(issue)).thenReturn(saved);
      when(issueMapper.toIssueResponse(saved)).thenReturn(response);

      IssueResponse result = issueService.addIssue(request, creatorId);

      assertThat(result).isEqualTo(response);
      verify(issue).setIssueType(issueType);
      verify(issue).setCreator(creator);
      verify(issue).setTeam(team);
      verify(issueRepository).save(issue);
    }

    @Test
    void addIssue_nullIssueType_setsCreatorAndTeamAndReturnsResponse() {
      UUID creatorId = UUID.randomUUID();
      UUID teamId = UUID.randomUUID();
      CreateIssueRequest request = new CreateIssueRequest("Bug", "desc", null, teamId);
      Issue issue = org.mockito.Mockito.mock(Issue.class);
      Team team = org.mockito.Mockito.mock(Team.class);
      User creator = org.mockito.Mockito.mock(User.class);
      Issue saved = org.mockito.Mockito.mock(Issue.class);
      IssueResponse response =
          new IssueResponse(
              UUID.randomUUID(), "Bug", "desc", null, false, null, null, null, null, null, null);

      when(issueMapper.toEntity(request)).thenReturn(issue);
      when(userService.getUserById(creatorId)).thenReturn(creator);
      when(teamService.getTeamById(teamId)).thenReturn(team);
      when(issueRepository.save(issue)).thenReturn(saved);
      when(issueMapper.toIssueResponse(saved)).thenReturn(response);

      IssueResponse result = issueService.addIssue(request, creatorId);

      assertThat(result).isEqualTo(response);
      verify(issue).setCreator(creator);
      verify(issue).setTeam(team);
      verify(issueRepository).save(issue);
    }

    @Test
    void addIssue_missingTeam_throwsResourceNotFoundException() {
      UUID creatorId = UUID.randomUUID();
      UUID teamId = UUID.randomUUID();
      CreateIssueRequest request = new CreateIssueRequest("Bug", "desc", null, teamId);
      Issue issue = org.mockito.Mockito.mock(Issue.class);
      User creator = org.mockito.Mockito.mock(User.class);

      when(issueMapper.toEntity(request)).thenReturn(issue);
      when(userService.getUserById(creatorId)).thenReturn(creator);
      when(teamService.getTeamById(teamId))
          .thenThrow(new ResourceNotFoundException("Team not found with id: " + teamId));

      assertThatThrownBy(() -> issueService.addIssue(request, creatorId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));

      verify(issueRepository, never()).save(any(Issue.class));
    }
  }

  @Nested
  class GetIssuesByTeam {
    @Test
    void getIssuesByTeam_emptyPage_returnsEmptyData() {
      UUID teamId = UUID.randomUUID();
      when(issueRepository.findIssueIdsByTeamId(eq(teamId), eq(true), any(Pageable.class)))
          .thenReturn(Page.empty(PageRequest.of(0, 10)));

      PagedEntityResponse<IssueResponse> result =
          issueService.getIssuesByTeam(teamId, new PaginationRequest(1, 10), null, true);

      assertThat(result.data()).isEmpty();
      verify(teamService).validateTeamExists(teamId);
      verify(issueRepository)
          .findIssueIdsByTeamId(
              eq(teamId), eq(true), argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10));
    }

    @Test
    void getIssuesByTeam_invalidTypeFilter_throwsBadRequestException() {
      UUID teamId = UUID.randomUUID();

      assertThatThrownBy(
              () ->
                  issueService.getIssuesByTeam(
                      teamId, new PaginationRequest(1, 10), "bad-uuid", false))
          .isInstanceOf(BadRequestException.class)
          .satisfies(
              ex ->
                  assertThat(((BadRequestException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.BAD_REQUEST));
    }

    @Test
    void getIssuesByTeam_withNoneTypeFilter_usesNullTypeQuery() {
      UUID teamId = UUID.randomUUID();
      UUID issueId = UUID.randomUUID();
      Page<UUID> idsPage = new PageImpl<>(List.of(issueId), PageRequest.of(0, 10), 1);
      Issue issue = org.mockito.Mockito.mock(Issue.class);
      IssueResponse response =
          new IssueResponse(
              issueId, "Bug", "desc", null, false, null, null, null, null, null, null);

      when(issueRepository.findIssueIdsByTeamIdAndIssueTypeIsNull(
              eq(teamId), eq(false), any(Pageable.class)))
          .thenReturn(idsPage);
      when(issueRepository.findAllByIdIn(List.of(issueId))).thenReturn(List.of(issue));
      when(issue.getId()).thenReturn(issueId);
      when(issueMapper.toIssueResponse(issue)).thenReturn(response);

      PagedEntityResponse<IssueResponse> result =
          issueService.getIssuesByTeam(teamId, new PaginationRequest(1, 10), "none", false);

      assertThat(result.data()).containsExactly(response);
      verify(issueRepository)
          .findIssueIdsByTeamIdAndIssueTypeIsNull(
              eq(teamId),
              eq(false),
              argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10));
    }

    @Test
    void getIssuesByTeam_validTypeFilter_usesTypedQueryAndReturnsData() {
      UUID teamId = UUID.randomUUID();
      UUID issueId = UUID.randomUUID();
      UUID issueTypeId = UUID.randomUUID();
      Page<UUID> idsPage = new PageImpl<>(List.of(issueId), PageRequest.of(0, 10), 1);
      Issue issue = org.mockito.Mockito.mock(Issue.class);
      IssueResponse response =
          new IssueResponse(
              issueId, "Bug", "desc", null, false, null, null, null, null, null, null);

      when(issueRepository.findIssueIdsByTeamIdAndIssueTypeId(
              eq(teamId), eq(issueTypeId), eq(false), any(Pageable.class)))
          .thenReturn(idsPage);
      when(issueRepository.findAllByIdIn(List.of(issueId))).thenReturn(List.of(issue));
      when(issue.getId()).thenReturn(issueId);
      when(issueMapper.toIssueResponse(issue)).thenReturn(response);

      PagedEntityResponse<IssueResponse> result =
          issueService.getIssuesByTeam(teamId, new PaginationRequest(1, 10), issueTypeId.toString(), false);

      assertThat(result.data()).containsExactly(response);
      verify(issueRepository)
          .findIssueIdsByTeamIdAndIssueTypeId(
              eq(teamId),
              eq(issueTypeId),
              eq(false),
              argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10));
    }
  }

  @Nested
  class UpdateIssue {
    @Test
    void updateIssue_archivedIssue_throwsConflictException() {
      UUID issueId = UUID.randomUUID();
      Issue issue = org.mockito.Mockito.mock(Issue.class);
      when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
      when(issue.getIsArchived()).thenReturn(true);

      assertThatThrownBy(
              () -> issueService.updateIssue(issueId, new UpdateIssueRequest("T", "D", null)))
          .isInstanceOf(ConflictException.class)
          .satisfies(
              ex ->
                  assertThat(((ConflictException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.CONFLICT));

      verify(issueRepository, never()).save(any(Issue.class));
    }

    @Test
    void updateIssue_nullIssueTypeId_clearsIssueTypeAndSaves() {
      UUID issueId = UUID.randomUUID();
      Issue issue = org.mockito.Mockito.mock(Issue.class);
      Issue saved = org.mockito.Mockito.mock(Issue.class);
      IssueResponse response =
          new IssueResponse(issueId, "T", "D", null, false, null, null, null, null, null, null);

      when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
      when(issue.getIsArchived()).thenReturn(false);
      when(issueRepository.save(issue)).thenReturn(saved);
      when(issueMapper.toIssueResponse(saved)).thenReturn(response);

      IssueResponse result =
          issueService.updateIssue(issueId, new UpdateIssueRequest("T", "D", null));

      assertThat(result).isEqualTo(response);
      verify(issue).setIssueType(null);
      verify(issueRepository).save(issue);
    }

    @Test
    void updateIssue_missingIssue_throwsResourceNotFoundException() {
      UUID issueId = UUID.randomUUID();
      when(issueRepository.findById(issueId)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> issueService.updateIssue(issueId, new UpdateIssueRequest("T", "D", null)))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));

      verify(issueRepository, never()).save(any(Issue.class));
    }
  }

  @Nested
  class ArchiveAndDelete {
    @Test
    void updateIssueArchiveStatus_sameStatus_returnsWithoutSave() {
      UUID issueId = UUID.randomUUID();
      Issue issue = org.mockito.Mockito.mock(Issue.class);
      IssueResponse response =
          new IssueResponse(issueId, "T", "D", null, false, null, null, null, null, null, null);
      when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
      when(issue.getIsArchived()).thenReturn(false);
      when(issueMapper.toIssueResponse(issue)).thenReturn(response);

      IssueResponse result = issueService.updateIssueArchiveStatus(issueId, false);

      assertThat(result).isEqualTo(response);
      verify(issueRepository, never()).save(any(Issue.class));
    }

    @Test
    void deleteIssueById_missingIssue_throwsResourceNotFoundException() {
      UUID issueId = UUID.randomUUID();
      when(issueRepository.findById(issueId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> issueService.deleteIssueById(issueId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void deleteIssueById_existingIssue_deletesSuccessfully() {
      UUID issueId = UUID.randomUUID();
      Issue issue = org.mockito.Mockito.mock(Issue.class);
      when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));

      issueService.deleteIssueById(issueId);

      verify(issueRepository).delete(issue);
    }
  }
}
