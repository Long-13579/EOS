package com.ces.eos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ces.eos.dto.request.CreateL10MeetingRequest;
import com.ces.eos.dto.request.PaginationRequest;
import com.ces.eos.dto.request.UpdateL10MeetingConcludeRequest;
import com.ces.eos.dto.request.UpdateL10MeetingRequest;
import com.ces.eos.dto.request.UpsertL10MeetingRatingsRequest;
import com.ces.eos.dto.response.L10MeetingResponse;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.entity.L10Meeting;
import com.ces.eos.entity.L10MeetingRating;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.User;
import com.ces.eos.enums.AiSummaryStatus;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.enums.L10MeetingRatingValue;
import com.ces.eos.enums.L10MeetingStatus;
import com.ces.eos.exception.AuthException;
import com.ces.eos.exception.ConflictException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.L10MeetingMapper;
import com.ces.eos.mapper.L10MeetingRatingMapper;
import com.ces.eos.repository.L10MeetingRatingRepository;
import com.ces.eos.repository.L10MeetingRepository;
import com.ces.eos.service.L10MeetingChangeLogService;
import com.ces.eos.service.TeamService;
import com.ces.eos.service.UserService;
import com.ces.eos.util.ChangeLogDiffExtractor;
import com.ces.eos.util.DateUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class L10MeetingServiceImplTest {

  @Mock private L10MeetingRepository l10MeetingRepository;
  @Mock private L10MeetingRatingRepository l10MeetingRatingRepository;
  @Mock private L10MeetingMapper l10MeetingMapper;
  @Mock private L10MeetingRatingMapper l10MeetingRatingMapper;
  @Mock private TeamService teamService;
  @Mock private UserService userService;
  @Mock private L10MeetingChangeLogService l10MeetingChangeLogService;
  @Mock private ChangeLogDiffExtractor changeLogDiffExtractor;
  @Mock private AiSummaryGenerator aiSummaryGenerator;
  @Mock private ObjectMapper objectMapper;

  @InjectMocks private L10MeetingServiceImpl l10MeetingService;

  private MockedStatic<DateUtils> dateUtils;
  private MockedStatic<TransactionSynchronizationManager> transactionSyncManager;

  @BeforeEach
  void setUp() {
    dateUtils = mockStatic(DateUtils.class);
    transactionSyncManager = mockStatic(TransactionSynchronizationManager.class);
    transactionSyncManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
    transactionSyncManager.when(() -> TransactionSynchronizationManager.registerSynchronization(any()))
        .thenAnswer(invocation -> {
          var sync = invocation.getArgument(0, org.springframework.transaction.support.TransactionSynchronization.class);
          return null;
        });
    lenient().when(objectMapper.valueToTree(any())).thenReturn(mock(JsonNode.class));
  }

  @AfterEach
  void tearDown() {
    dateUtils.close();
    transactionSyncManager.close();
  }

  private Team createTeam(UUID id) {
    return Team.builder().id(id).timezone("UTC").build();
  }

  private User createUser(UUID id) {
    return User.builder().id(id).firstName("Test").lastName("User").email("test@example.com").build();
  }

  private L10Meeting createMeeting(UUID id, Team team, L10MeetingStatus status, User facilitator, User scribe, LocalDate meetingDate) {
    return L10Meeting.builder()
        .id(id)
        .team(team)
        .meetingDate(meetingDate)
        .meetingTime(LocalTime.of(10, 0))
        .weekStartDate(meetingDate.with(java.time.DayOfWeek.MONDAY))
        .facilitator(facilitator)
        .scribe(scribe)
        .status(status)
        .build();
  }

  @Nested
  class ScheduleMeeting {

    @Test
    void scheduleMeeting_validRequest_returnsMappedResponse() {
      UUID teamId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID scribeId = UUID.randomUUID();
      UUID schedulerId = UUID.randomUUID();
      LocalDate meetingDate = LocalDate.of(2026, 6, 1);
      LocalTime meetingTime = LocalTime.of(10, 0);
      LocalDate weekStart = LocalDate.of(2026, 6, 1);

      CreateL10MeetingRequest request = new CreateL10MeetingRequest(teamId, meetingDate, meetingTime, facilitatorId, scribeId);
      Team team = createTeam(teamId);
      User facilitator = createUser(facilitatorId);
      User scribe = createUser(scribeId);
      L10Meeting meeting = L10Meeting.builder().build();
      L10Meeting saved = L10Meeting.builder().id(UUID.randomUUID()).build();
      L10MeetingResponse response = new L10MeetingResponse(saved.getId(), null, meetingDate, meetingTime, weekStart, null, null, L10MeetingStatus.SCHEDULED, null, null, null, null, null, null, null, null, null);

      when(teamService.getTeamById(teamId)).thenReturn(team);
      when(userService.getUserByIdAndTeamId(facilitatorId, teamId)).thenReturn(facilitator);
      when(userService.getUserByIdAndTeamId(scribeId, teamId)).thenReturn(scribe);
      dateUtils.when(() -> DateUtils.getWeekStartDate(meetingDate, meetingTime, "UTC")).thenReturn(weekStart);
      when(l10MeetingRepository.findByTeam_IdAndWeekStartDate(teamId, weekStart)).thenReturn(Optional.empty());
      when(l10MeetingMapper.toEntity(request)).thenReturn(meeting);
      when(l10MeetingRepository.save(any())).thenAnswer(invocation -> {
          L10Meeting m = invocation.getArgument(0);
          m.setId(saved.getId());
          return m;
      });
      when(l10MeetingRepository.findByIdWithRelations(any())).thenReturn(Optional.of(meeting));
      when(l10MeetingMapper.toL10MeetingResponse(meeting)).thenReturn(response);

      L10MeetingResponse result = l10MeetingService.scheduleMeeting(request, schedulerId);

      assertThat(result).isEqualTo(response);
      assertThat(meeting.getTeam()).isEqualTo(team);
      assertThat(meeting.getFacilitator()).isEqualTo(facilitator);
      assertThat(meeting.getScribe()).isEqualTo(scribe);
      assertThat(meeting.getWeekStartDate()).isEqualTo(weekStart);
      verify(l10MeetingRepository).save(meeting);
    }

    @Test
    void scheduleMeeting_duplicateWeek_throwsConflictException() {
      UUID teamId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID scribeId = UUID.randomUUID();
      UUID schedulerId = UUID.randomUUID();
      LocalDate meetingDate = LocalDate.of(2026, 6, 1);
      LocalTime meetingTime = LocalTime.of(10, 0);
      LocalDate weekStart = LocalDate.of(2026, 6, 1);

      CreateL10MeetingRequest request = new CreateL10MeetingRequest(teamId, meetingDate, meetingTime, facilitatorId, scribeId);
      Team team = createTeam(teamId);
      User facilitator = createUser(facilitatorId);
      User scribe = createUser(scribeId);

      when(teamService.getTeamById(teamId)).thenReturn(team);
      when(userService.getUserByIdAndTeamId(facilitatorId, teamId)).thenReturn(facilitator);
      when(userService.getUserByIdAndTeamId(scribeId, teamId)).thenReturn(scribe);
      dateUtils.when(() -> DateUtils.getWeekStartDate(meetingDate, meetingTime, "UTC")).thenReturn(weekStart);
      when(l10MeetingRepository.findByTeam_IdAndWeekStartDate(teamId, weekStart)).thenReturn(Optional.of(L10Meeting.builder().build()));

      assertThatThrownBy(() -> l10MeetingService.scheduleMeeting(request, schedulerId))
          .isInstanceOf(ConflictException.class)
          .satisfies(ex -> assertThat(((ConflictException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

      verify(l10MeetingRepository, never()).save(any());
    }
  }

  @Nested
  class StartMeeting {

    @Test
    void startMeeting_scheduledMeetingOnSameDay_startsSuccessfully() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      User scribe = createUser(UUID.randomUUID());
      LocalDate today = LocalDate.of(2026, 6, 1);
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.SCHEDULED, facilitator, scribe, today);
      L10MeetingResponse response = new L10MeetingResponse(meetingId, null, today, LocalTime.of(10, 0), today, null, null, L10MeetingStatus.STARTED, null, null, null, null, null, null, null, null, null);

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));
      dateUtils.when(() -> DateUtils.getTodayForTimezone("UTC")).thenReturn(today);
      when(l10MeetingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
      when(l10MeetingMapper.toL10MeetingResponse(any())).thenReturn(response);

      L10MeetingResponse result = l10MeetingService.startMeeting(meetingId, userId);

      assertThat(result).isEqualTo(response);
      assertThat(meeting.getStatus()).isEqualTo(L10MeetingStatus.STARTED);
      verify(l10MeetingRepository).save(meeting);
    }

    @Test
    void startMeeting_alreadyStarted_throwsConflictException() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.STARTED, facilitator, createUser(UUID.randomUUID()), LocalDate.now());

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));

      assertThatThrownBy(() -> l10MeetingService.startMeeting(meetingId, userId))
          .isInstanceOf(ConflictException.class)
          .satisfies(ex -> assertThat(((ConflictException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

      verify(l10MeetingRepository, never()).save(any());
    }

    @Test
    void startMeeting_finishedMeeting_throwsConflictException() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.FINISHED, facilitator, createUser(UUID.randomUUID()), LocalDate.now());

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));

      assertThatThrownBy(() -> l10MeetingService.startMeeting(meetingId, userId))
          .isInstanceOf(ConflictException.class)
          .satisfies(ex -> assertThat(((ConflictException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

      verify(l10MeetingRepository, never()).save(any());
    }

    @Test
    void startMeeting_notScheduledDate_throwsConflictException() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      User scribe = createUser(UUID.randomUUID());
      LocalDate meetingDate = LocalDate.of(2026, 6, 1);
      LocalDate today = LocalDate.of(2026, 6, 5);
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.SCHEDULED, facilitator, scribe, meetingDate);

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));
      dateUtils.when(() -> DateUtils.getTodayForTimezone("UTC")).thenReturn(today);

      assertThatThrownBy(() -> l10MeetingService.startMeeting(meetingId, userId))
          .isInstanceOf(ConflictException.class)
          .satisfies(ex -> assertThat(((ConflictException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

      verify(l10MeetingRepository, never()).save(any());
    }

    @Test
    void startMeeting_notFacilitatorOrScribe_throwsAuthException() {
      UUID meetingId = UUID.randomUUID();
      UUID otherUserId = UUID.randomUUID();
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(UUID.randomUUID());
      User scribe = createUser(UUID.randomUUID());
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.SCHEDULED, facilitator, scribe, LocalDate.now());

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));

      assertThatThrownBy(() -> l10MeetingService.startMeeting(meetingId, otherUserId))
          .isInstanceOf(AuthException.class)
          .satisfies(ex -> assertThat(((AuthException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

      verify(l10MeetingRepository, never()).save(any());
    }

    @Test
    void startMeeting_nullUserId_throwsAuthException() {
      UUID meetingId = UUID.randomUUID();
      Team team = createTeam(UUID.randomUUID());
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.SCHEDULED, createUser(UUID.randomUUID()), createUser(UUID.randomUUID()), LocalDate.now());

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));

      assertThatThrownBy(() -> l10MeetingService.startMeeting(meetingId, null))
          .isInstanceOf(AuthException.class)
          .satisfies(ex -> assertThat(((AuthException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

      verify(l10MeetingRepository, never()).save(any());
    }
  }

  @Nested
  class FinishMeeting {

    @Test
    void finishMeeting_startedMeeting_finishesSuccessfully() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      User scribe = createUser(UUID.randomUUID());
      LocalDate today = LocalDate.of(2026, 6, 1);
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.STARTED, facilitator, scribe, today);
      L10MeetingResponse response = new L10MeetingResponse(meetingId, null, today, LocalTime.of(10, 0), today, null, null, L10MeetingStatus.FINISHED, null, null, null, null, null, null, null, null, AiSummaryStatus.PENDING);

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));
      when(l10MeetingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
      when(l10MeetingMapper.toL10MeetingResponse(any())).thenReturn(response);

      L10MeetingResponse result = l10MeetingService.finishMeeting(meetingId, userId);

      assertThat(result).isEqualTo(response);
      assertThat(meeting.getStatus()).isEqualTo(L10MeetingStatus.FINISHED);
      assertThat(meeting.getAiSummary()).isNull();
      assertThat(meeting.getAiSummaryStatus()).isEqualTo(AiSummaryStatus.PENDING);
      verify(l10MeetingRepository).save(meeting);
    }

    @Test
    void finishMeeting_scheduledMeeting_throwsConflictException() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.SCHEDULED, facilitator, createUser(UUID.randomUUID()), LocalDate.now());

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));

      assertThatThrownBy(() -> l10MeetingService.finishMeeting(meetingId, userId))
          .isInstanceOf(ConflictException.class)
          .satisfies(ex -> assertThat(((ConflictException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

      verify(l10MeetingRepository, never()).save(any());
    }
  }

  @Nested
  class RegenerateSummary {

    @Test
    void regenerateSummary_failedMeeting_resetsToPendingAndSaves() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      User scribe = createUser(UUID.randomUUID());
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.FINISHED, facilitator, scribe, LocalDate.now());
      meeting.setAiSummaryStatus(AiSummaryStatus.FAILED);
      meeting.setAiSummary("Fallback summary");
      L10MeetingResponse response = new L10MeetingResponse(meetingId, null, null, null, null, null, null, L10MeetingStatus.FINISHED, null, null, null, null, null, null, null, null, AiSummaryStatus.PENDING);

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));
      when(l10MeetingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
      when(l10MeetingMapper.toL10MeetingResponse(any())).thenReturn(response);

      L10MeetingResponse result = l10MeetingService.regenerateSummary(meetingId, userId);

      assertThat(result).isEqualTo(response);
      assertThat(meeting.getAiSummary()).isNull();
      assertThat(meeting.getAiSummaryStatus()).isEqualTo(AiSummaryStatus.PENDING);
      verify(l10MeetingRepository).save(meeting);
    }

    @Test
    void regenerateSummary_notFailed_throwsConflictException() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      User scribe = createUser(UUID.randomUUID());
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.FINISHED, facilitator, scribe, LocalDate.now());
      meeting.setAiSummaryStatus(AiSummaryStatus.COMPLETED);

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));

      assertThatThrownBy(() -> l10MeetingService.regenerateSummary(meetingId, userId))
          .isInstanceOf(ConflictException.class)
          .satisfies(ex -> assertThat(((ConflictException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

      verify(l10MeetingRepository, never()).save(any());
    }
  }

  @Nested
  class UpdateConclude {

    @Test
    void updateConclude_startedMeeting_savesConcludeNotes() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      User scribe = createUser(UUID.randomUUID());
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.STARTED, facilitator, scribe, LocalDate.now());
      UpdateL10MeetingConcludeRequest request = new UpdateL10MeetingConcludeRequest("Key decisions", "Cascading message");
      L10MeetingResponse response = new L10MeetingResponse(meetingId, null, null, null, null, null, null, L10MeetingStatus.STARTED, "Key decisions", "Cascading message", null, null, null, null, null, null, null);

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));
      when(l10MeetingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
      when(l10MeetingMapper.toL10MeetingResponse(any())).thenReturn(response);

      L10MeetingResponse result = l10MeetingService.updateConclude(meetingId, request, userId);

      assertThat(result).isEqualTo(response);
      assertThat(meeting.getConcludeKeyDecisions()).isEqualTo("Key decisions");
      assertThat(meeting.getConcludeCascadingMessage()).isEqualTo("Cascading message");
    }

    @Test
    void updateConclude_finishedMeeting_throwsConflictException() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.FINISHED, facilitator, createUser(UUID.randomUUID()), LocalDate.now());
      UpdateL10MeetingConcludeRequest request = new UpdateL10MeetingConcludeRequest("Key decisions", "Cascading message");

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));

      assertThatThrownBy(() -> l10MeetingService.updateConclude(meetingId, request, userId))
          .isInstanceOf(ConflictException.class)
          .satisfies(ex -> assertThat(((ConflictException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

      verify(l10MeetingRepository, never()).save(any());
    }
  }

  @Nested
  class UpsertRatings {

    @Test
    void upsertRatings_startedMeeting_savesRatings() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID userId = facilitatorId;
      UUID memberId = UUID.randomUUID();
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      User scribe = createUser(UUID.randomUUID());
      User member = createUser(memberId);
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.STARTED, facilitator, scribe, LocalDate.now());

      var ratingItem = new com.ces.eos.dto.request.L10MeetingRatingItemRequest(memberId, "FIVE");
      UpsertL10MeetingRatingsRequest request = new UpsertL10MeetingRatingsRequest(List.of(ratingItem));

      L10MeetingRating savedRating = L10MeetingRating.builder()
          .id(UUID.randomUUID())
          .meeting(meeting)
          .member(member)
          .rating(L10MeetingRatingValue.FIVE)
          .build();

      var ratingResponse = new com.ces.eos.dto.response.L10MeetingRatingResponse(
          savedRating.getId(), meetingId, null, "FIVE", null, null);

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));
      when(teamService.getActiveUsersByTeamId(team.getId())).thenReturn(List.of(member));
      when(l10MeetingRatingRepository.findByMeeting_IdAndMember_Id(meetingId, memberId)).thenReturn(Optional.empty());
      when(l10MeetingRatingRepository.save(any(L10MeetingRating.class))).thenReturn(savedRating);
      when(l10MeetingRatingMapper.toL10MeetingRatingResponse(any())).thenReturn(ratingResponse);

      var result = l10MeetingService.upsertRatings(meetingId, userId, request);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().rating()).isEqualTo("FIVE");
    }

    @Test
    void upsertRatings_finishedMeeting_throwsConflictException() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.FINISHED, facilitator, createUser(UUID.randomUUID()), LocalDate.now());
      var request = new UpsertL10MeetingRatingsRequest(List.of());

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));

      assertThatThrownBy(() -> l10MeetingService.upsertRatings(meetingId, userId, request))
          .isInstanceOf(ConflictException.class)
          .satisfies(ex -> assertThat(((ConflictException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

      verify(l10MeetingRatingRepository, never()).save(any());
    }

    @Test
    void upsertRatings_memberCountMismatch_throwsConflictException() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      User member1 = createUser(UUID.randomUUID());
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.STARTED, facilitator, createUser(UUID.randomUUID()), LocalDate.now());
      var request = new UpsertL10MeetingRatingsRequest(List.of());

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));
      when(teamService.getActiveUsersByTeamId(team.getId())).thenReturn(List.of(member1));

      assertThatThrownBy(() -> l10MeetingService.upsertRatings(meetingId, userId, request))
          .isInstanceOf(ConflictException.class)
          .satisfies(ex -> assertThat(((ConflictException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
    }
  }

  @Nested
  class DeleteMeeting {

    @Test
    void deleteMeeting_scheduledMeeting_deletesSuccessfully() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      User scribe = createUser(UUID.randomUUID());
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.SCHEDULED, facilitator, scribe, LocalDate.now());

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));

      l10MeetingService.deleteMeeting(meetingId, userId);

      verify(l10MeetingRepository).delete(meeting);
    }

    @Test
    void deleteMeeting_startedMeeting_throwsConflictException() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.STARTED, facilitator, createUser(UUID.randomUUID()), LocalDate.now());

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));

      assertThatThrownBy(() -> l10MeetingService.deleteMeeting(meetingId, userId))
          .isInstanceOf(ConflictException.class)
          .satisfies(ex -> assertThat(((ConflictException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

      verify(l10MeetingRepository, never()).delete(any());
    }

    @Test
    void deleteMeeting_finishedMeeting_throwsConflictException() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.FINISHED, facilitator, createUser(UUID.randomUUID()), LocalDate.now());

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));

      assertThatThrownBy(() -> l10MeetingService.deleteMeeting(meetingId, userId))
          .isInstanceOf(ConflictException.class)
          .satisfies(ex -> assertThat(((ConflictException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

      verify(l10MeetingRepository, never()).delete(any());
    }
  }

  @Nested
  class UpdateMeeting {

    @Test
    void updateMeeting_scheduledMeetingSameWeek_updatesSuccessfully() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID scribeId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      User scribe = createUser(scribeId);
      LocalDate date = LocalDate.of(2026, 6, 1);
      LocalTime time = LocalTime.of(10, 0);
      LocalDate weekStart = LocalDate.of(2026, 6, 1);
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.SCHEDULED, facilitator, scribe, date);
      UpdateL10MeetingRequest request = new UpdateL10MeetingRequest(date, time, facilitatorId, scribeId);

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));
      dateUtils.when(() -> DateUtils.getWeekStartDate(date, time, "UTC")).thenReturn(weekStart);
      when(userService.getUserByIdAndTeamId(facilitatorId, team.getId())).thenReturn(facilitator);
      when(userService.getUserByIdAndTeamId(scribeId, team.getId())).thenReturn(scribe);
      when(l10MeetingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
      L10MeetingResponse response = new L10MeetingResponse(meetingId, null, date, time, weekStart, null, null, L10MeetingStatus.SCHEDULED, null, null, null, null, null, null, null, null, null);
      when(l10MeetingMapper.toL10MeetingResponse(any())).thenReturn(response);

      L10MeetingResponse result = l10MeetingService.updateMeeting(meetingId, request, userId);

      assertThat(result).isEqualTo(response);
      verify(l10MeetingRepository).save(meeting);
    }

    @Test
    void updateMeeting_differentWeekWithConflict_throwsConflictException() {
      UUID meetingId = UUID.randomUUID();
      UUID facilitatorId = UUID.randomUUID();
      UUID scribeId = UUID.randomUUID();
      UUID userId = facilitatorId;
      Team team = createTeam(UUID.randomUUID());
      User facilitator = createUser(facilitatorId);
      User scribe = createUser(scribeId);
      LocalDate originalDate = LocalDate.of(2026, 6, 1);
      LocalDate newDate = LocalDate.of(2026, 6, 8);
      LocalTime time = LocalTime.of(10, 0);
      LocalDate newWeekStart = LocalDate.of(2026, 6, 8);
      L10Meeting meeting = createMeeting(meetingId, team, L10MeetingStatus.SCHEDULED, facilitator, scribe, originalDate);
      meeting.setWeekStartDate(LocalDate.of(2026, 6, 1));
      UpdateL10MeetingRequest request = new UpdateL10MeetingRequest(newDate, time, facilitatorId, scribeId);

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));
      dateUtils.when(() -> DateUtils.getWeekStartDate(newDate, time, "UTC")).thenReturn(newWeekStart);
      when(l10MeetingRepository.findByTeam_IdAndWeekStartDate(team.getId(), newWeekStart))
          .thenReturn(Optional.of(L10Meeting.builder().id(UUID.randomUUID()).build()));

      assertThatThrownBy(() -> l10MeetingService.updateMeeting(meetingId, request, userId))
          .isInstanceOf(ConflictException.class)
          .satisfies(ex -> assertThat(((ConflictException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));

      verify(l10MeetingRepository, never()).save(any());
    }
  }

  @Nested
  class GetMeetingsByTeam {

    @Test
    void getMeetingsByTeam_emptyIdsPage_returnsEmptyPagedResponse() {
      UUID teamId = UUID.randomUUID();
      PaginationRequest request = new PaginationRequest(1, 10);
      when(l10MeetingRepository.findMeetingIdsByTeamIdAndStatus(any(), any(), any()))
          .thenReturn(Page.empty(PageRequest.of(0, 10)));

      PagedEntityResponse<L10MeetingResponse> result =
          l10MeetingService.getMeetingsByTeam(teamId, List.of(L10MeetingStatus.FINISHED), request);

      assertThat(result.data()).isEmpty();
      verify(teamService).validateTeamExists(teamId);
    }

    @Test
    void getMeetingsByTeam_withFinishedIds_returnsMappedItems() {
      UUID teamId = UUID.randomUUID();
      UUID id1 = UUID.randomUUID();
      UUID id2 = UUID.randomUUID();
      PaginationRequest request = new PaginationRequest(1, 10);
      Page<UUID> idsPage = new PageImpl<>(List.of(id1, id2), PageRequest.of(0, 10), 2);
      L10Meeting m1 = L10Meeting.builder().id(id1).status(L10MeetingStatus.FINISHED).build();
      L10Meeting m2 = L10Meeting.builder().id(id2).status(L10MeetingStatus.FINISHED).build();
      L10MeetingResponse r1 = new L10MeetingResponse(id1, null, null, null, null, null, null, L10MeetingStatus.FINISHED, null, null, null, null, null, null, null, null, null);
      L10MeetingResponse r2 = new L10MeetingResponse(id2, null, null, null, null, null, null, L10MeetingStatus.FINISHED, null, null, null, null, null, null, null, null, null);

      when(l10MeetingRepository.findMeetingIdsByTeamIdAndStatus(any(), any(), any())).thenReturn(idsPage);
      when(l10MeetingRepository.findAllByIdIn(List.of(id1, id2))).thenReturn(List.of(m1, m2));
      when(l10MeetingMapper.toL10MeetingResponse(m1)).thenReturn(r1);
      when(l10MeetingMapper.toL10MeetingResponse(m2)).thenReturn(r2);

      PagedEntityResponse<L10MeetingResponse> result =
          l10MeetingService.getMeetingsByTeam(teamId, List.of(L10MeetingStatus.FINISHED), request);

      assertThat(result.data()).containsExactly(r1, r2);
    }

    @Test
    void getMeetingsByTeam_upcomingStatuses_returnsMappedItemsInStartedFirstOrder() {
      UUID teamId = UUID.randomUUID();
      UUID id1 = UUID.randomUUID();
      PaginationRequest request = new PaginationRequest(1, 10);
      Page<UUID> idsPage = new PageImpl<>(List.of(id1), PageRequest.of(0, 10), 1);
      L10Meeting m1 = L10Meeting.builder().id(id1).status(L10MeetingStatus.SCHEDULED).build();
      L10MeetingResponse r1 = new L10MeetingResponse(id1, null, null, null, null, null, null, L10MeetingStatus.SCHEDULED, null, null, null, null, null, null, null, null, null);

      when(l10MeetingRepository.findMeetingIdsByTeamIdAndStatusIn(any(), any(), any())).thenReturn(idsPage);
      when(l10MeetingRepository.findAllByIdIn(List.of(id1))).thenReturn(List.of(m1));
      when(l10MeetingMapper.toL10MeetingResponse(m1)).thenReturn(r1);

      PagedEntityResponse<L10MeetingResponse> result =
          l10MeetingService.getMeetingsByTeam(teamId, List.of(L10MeetingStatus.SCHEDULED, L10MeetingStatus.STARTED), request);

      assertThat(result.data()).containsExactly(r1);
    }
  }

  @Nested
  class GetMeeting {

    @Test
    void getMeeting_existingId_returnsMapping() {
      UUID meetingId = UUID.randomUUID();
      L10Meeting meeting = L10Meeting.builder().id(meetingId).build();
      L10MeetingResponse response = new L10MeetingResponse(meetingId, null, null, null, null, null, null, L10MeetingStatus.SCHEDULED, null, null, null, null, null, null, null, null, null);

      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.of(meeting));
      when(l10MeetingMapper.toL10MeetingResponse(meeting)).thenReturn(response);

      L10MeetingResponse result = l10MeetingService.getMeeting(meetingId);

      assertThat(result).isEqualTo(response);
    }

    @Test
    void getMeeting_missingId_throwsResourceNotFoundException() {
      UUID meetingId = UUID.randomUUID();
      when(l10MeetingRepository.findByIdWithRelations(meetingId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> l10MeetingService.getMeeting(meetingId))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(ex -> assertThat(((ResourceNotFoundException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }
  }
}
