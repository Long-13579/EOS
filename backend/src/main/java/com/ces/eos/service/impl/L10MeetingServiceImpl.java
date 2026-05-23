package com.ces.eos.service.impl;

import com.ces.eos.dto.request.CreateL10MeetingRequest;
import com.ces.eos.dto.request.PaginationRequest;
import com.ces.eos.dto.request.UpdateL10MeetingConcludeRequest;
import com.ces.eos.dto.request.UpsertL10MeetingRatingsRequest;
import com.ces.eos.dto.response.L10MeetingRatingResponse;
import com.ces.eos.dto.response.L10MeetingResponse;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.entity.L10Meeting;
import com.ces.eos.entity.L10MeetingRating;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.User;
import com.ces.eos.enums.L10MeetingRatingValue;
import com.ces.eos.enums.L10MeetingStatus;
import com.ces.eos.exception.AuthException;
import com.ces.eos.exception.ConflictException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.L10MeetingMapper;
import com.ces.eos.mapper.L10MeetingRatingMapper;
import com.ces.eos.repository.L10MeetingRatingRepository;
import com.ces.eos.repository.L10MeetingRepository;
import com.ces.eos.service.L10MeetingService;
import com.ces.eos.service.TeamService;
import com.ces.eos.service.UserService;
import com.ces.eos.util.DateUtils;
import com.ces.eos.util.EnumParserUtil;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class L10MeetingServiceImpl implements L10MeetingService {

  private final L10MeetingRepository l10MeetingRepository;
  private final L10MeetingRatingRepository l10MeetingRatingRepository;
  private final L10MeetingMapper l10MeetingMapper;
  private final L10MeetingRatingMapper l10MeetingRatingMapper;
  private final TeamService teamService;
  private final UserService userService;

  @Override
  @Transactional
  public L10MeetingResponse scheduleMeeting(CreateL10MeetingRequest request, UUID schedulerId) {
    log.info("action=scheduleMeeting.start schedulerId={} teamId={}", schedulerId, request.teamId());
    Team team = teamService.getTeamById(request.teamId());
    User facilitator = userService.getUserByIdAndTeamId(request.facilitatorId(), request.teamId());
    User scribe = userService.getUserByIdAndTeamId(request.scribeId(), request.teamId());

    LocalDate weekStartDate =
        DateUtils.getWeekStartDate(request.meetingDate(), request.meetingTime(), team.getTimezone());
    validateMeetingWeekAvailable(team.getId(), weekStartDate);

    L10Meeting meeting = l10MeetingMapper.toEntity(request);
    meeting.setTeam(team);
    meeting.setFacilitator(facilitator);
    meeting.setScribe(scribe);
    meeting.setWeekStartDate(weekStartDate);

    L10Meeting savedMeeting = l10MeetingRepository.save(meeting);
    log.info("action=scheduleMeeting.success meetingId={}", savedMeeting.getId());
    return l10MeetingMapper.toL10MeetingResponse(loadMeetingWithRelations(savedMeeting.getId()));
  }

  @Override
  @Transactional
  public L10MeetingResponse startMeeting(UUID meetingId, UUID userId) {
    log.info("action=startMeeting.start meetingId={} userId={}", meetingId, userId);
    L10Meeting meeting = loadMeetingWithRelations(meetingId);
    ensureFacilitatorOrScribe(meeting, userId);

    if (meeting.getStatus() != L10MeetingStatus.SCHEDULED) {
      log.warn("action=startMeeting.validationFailed reason=status meetingId={} status={}",
          meetingId, meeting.getStatus());
      throw new ConflictException(
          Map.of(
              "meetingId",
              List.of("Meeting cannot be started again once it has already started.")));
    }

    meeting.setStatus(L10MeetingStatus.STARTED);
    L10Meeting savedMeeting = l10MeetingRepository.save(meeting);
    log.info("action=startMeeting.success meetingId={}", savedMeeting.getId());
    return l10MeetingMapper.toL10MeetingResponse(loadMeetingWithRelations(savedMeeting.getId()));
  }

  @Override
  @Transactional
  public L10MeetingResponse updateConclude(
      UUID meetingId, UpdateL10MeetingConcludeRequest request, UUID userId) {
    log.info("action=updateConclude.start meetingId={} userId={}", meetingId, userId);
    L10Meeting meeting = loadMeetingWithRelations(meetingId);
    ensureFacilitatorOrScribe(meeting, userId);

    if (meeting.getStatus() == L10MeetingStatus.FINISHED) {
      log.warn("action=updateConclude.validationFailed reason=finished meetingId={}", meetingId);
      throw new ConflictException(
          Map.of(
              "meetingId",
              List.of("Conclude notes are read-only after the meeting is finished.")));
    }

    meeting.setConcludeKeyDecisions(request.keyDecisions());
    meeting.setConcludeCascadingMessage(request.cascadingMessage());

    L10Meeting savedMeeting = l10MeetingRepository.save(meeting);
    log.info("action=updateConclude.success meetingId={}", savedMeeting.getId());
    return l10MeetingMapper.toL10MeetingResponse(loadMeetingWithRelations(savedMeeting.getId()));
  }

  @Override
  @Transactional
  public List<L10MeetingRatingResponse> upsertRatings(
      UUID meetingId, UUID userId, UpsertL10MeetingRatingsRequest request) {
    log.info("action=upsertRatings.start meetingId={} userId={}", meetingId, userId);
    L10Meeting meeting = loadMeetingWithRelations(meetingId);
    ensureFacilitatorOrScribe(meeting, userId);

    if (meeting.getStatus() == L10MeetingStatus.FINISHED) {
      log.warn("action=upsertRatings.validationFailed reason=finished meetingId={}", meetingId);
      throw new ConflictException(
          Map.of(
              "meetingId",
              List.of("Ratings are read-only after the meeting is finished.")));
    }

    List<User> activeMembers = teamService.getActiveUsersByTeamId(meeting.getTeam().getId());
    Map<UUID, User> activeMemberMap =
        activeMembers.stream().collect(Collectors.toMap(User::getId, Function.identity()));

    Map<UUID, L10MeetingRatingValue> requestedRatings =
        request.ratings().stream()
            .collect(
                Collectors.toMap(
                    rating -> rating.memberId(),
                    rating ->
                        EnumParserUtil.parseEnum(
                            L10MeetingRatingValue.class, rating.rating(), "rating")));

    validateRatingsMatchActiveMembers(activeMemberMap, requestedRatings, meetingId);

    List<L10MeetingRatingResponse> responses =
        activeMembers.stream()
            .map(
                member ->
                    upsertRatingForMember(
                        meeting,
                        member,
                        requestedRatings.getOrDefault(member.getId(), L10MeetingRatingValue.ABSENT)))
            .map(l10MeetingRatingMapper::toL10MeetingRatingResponse)
            .toList();

    log.info("action=upsertRatings.success meetingId={} count={}", meetingId, responses.size());
    return responses;
  }

  @Override
  public PagedEntityResponse<L10MeetingResponse> getMeetingsByTeam(
      UUID teamId, L10MeetingStatus status, PaginationRequest request) {
    log.info(
        "action=getMeetingsByTeam.start teamId={} status={} page={} limit={}",
        teamId, status, request.page(), request.limit());
    teamService.validateTeamExists(teamId);

    Sort sort;
    if (status == L10MeetingStatus.FINISHED) {
      sort = Sort.by(Sort.Order.desc("meetingDate"), Sort.Order.desc("meetingTime"), Sort.Order.desc("id"));
    } else {
      sort = Sort.by(Sort.Order.asc("meetingDate"), Sort.Order.asc("meetingTime"), Sort.Order.asc("id"));
    }

    Pageable pageable = PageRequest.of(request.page() - 1, request.limit(), sort);

    Page<UUID> meetingIdsPage =
        l10MeetingRepository.findMeetingIdsByTeamIdAndStatus(teamId, status, pageable);

    if (meetingIdsPage.isEmpty()) {
      log.info(
          "action=getMeetingsByTeam.success teamId={} status={} count=0", teamId, status);
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, L10Meeting> meetingMap =
        l10MeetingRepository.findAllByIdIn(meetingIdsPage.getContent()).stream()
            .collect(Collectors.toMap(L10Meeting::getId, Function.identity()));

    List<L10MeetingResponse> responses =
        meetingIdsPage.getContent().stream()
            .map(meetingMap::get)
            .filter(Objects::nonNull)
            .map(l10MeetingMapper::toL10MeetingResponse)
            .toList();

    log.info(
        "action=getMeetingsByTeam.success teamId={} status={} count={}",
        teamId, status, responses.size());
    return PagedEntityResponse.from(
        new PageImpl<>(responses, pageable, meetingIdsPage.getTotalElements()));
  }

  private void validateMeetingWeekAvailable(UUID teamId, LocalDate weekStartDate) {
    log.debug(
        "action=validateMeetingWeekAvailable.repo.findByTeam_IdAndWeekStartDate teamId={} weekStartDate={}",
        teamId,
        weekStartDate);
    if (l10MeetingRepository.findByTeam_IdAndWeekStartDate(teamId, weekStartDate).isPresent()) {
      log.warn("action=validateMeetingWeekAvailable.validationFailed teamId={} weekStartDate={}",
          teamId, weekStartDate);
      throw new ConflictException(
          Map.of(
              "teamId",
              List.of("An L10 meeting is already scheduled for that team and week.")));
    }
  }

  private void ensureFacilitatorOrScribe(L10Meeting meeting, UUID userId) {
    if (userId == null) {
      throw AuthException.forbidden("User is not authorized to perform this action.");
    }

    UUID facilitatorId = meeting.getFacilitator() != null ? meeting.getFacilitator().getId() : null;
    UUID scribeId = meeting.getScribe() != null ? meeting.getScribe().getId() : null;

    if (!userId.equals(facilitatorId) && !userId.equals(scribeId)) {
      log.warn("action=ensureFacilitatorOrScribe.validationFailed meetingId={} userId={}",
          meeting.getId(), userId);
      throw AuthException.forbidden("Only the facilitator or scribe can perform this action.");
    }
  }

  private L10Meeting loadMeetingWithRelations(UUID meetingId) {
    log.debug("action=loadMeetingWithRelations.repo.findByIdWithRelations meetingId={}", meetingId);
    return l10MeetingRepository
        .findByIdWithRelations(meetingId)
        .orElseThrow(
            () -> {
              log.warn("action=loadMeetingWithRelations.validationFailed meetingId={}", meetingId);
              return new ResourceNotFoundException(
                  Map.of(
                      "meetingId",
                      List.of(String.format("L10 meeting not found with id: %s", meetingId))));
            });
  }

  private void validateRatingsMatchActiveMembers(
      Map<UUID, User> activeMemberMap,
      Map<UUID, L10MeetingRatingValue> requestedRatings,
      UUID meetingId) {
    if (requestedRatings.size() != activeMemberMap.size()) {
      log.warn(
          "action=validateRatingsMatchActiveMembers.validationFailed reason=countMismatch meetingId={} expected={} received={}",
          meetingId,
          activeMemberMap.size(),
          requestedRatings.size());
      throw new ConflictException(
          Map.of(
              "ratings",
              List.of("Ratings must include all active team members for the meeting.")));
    }

    List<String> invalidMembers =
        requestedRatings.keySet().stream()
            .filter(memberId -> !activeMemberMap.containsKey(memberId))
            .map(memberId -> "Invalid member id: " + memberId)
            .toList();

    if (!invalidMembers.isEmpty()) {
      log.warn(
          "action=validateRatingsMatchActiveMembers.validationFailed reason=invalidMember meetingId={} count={}",
          meetingId,
          invalidMembers.size());
      throw new ConflictException(Map.of("memberIds", invalidMembers));
    }
  }

  private L10MeetingRating upsertRatingForMember(
      L10Meeting meeting, User member, L10MeetingRatingValue rating) {
    L10MeetingRating meetingRating =
        l10MeetingRatingRepository
            .findByMeeting_IdAndMember_Id(meeting.getId(), member.getId())
            .orElseGet(
                () ->
                    L10MeetingRating.builder()
                        .meeting(meeting)
                        .member(member)
                        .build());
    meetingRating.setRating(rating);
    return l10MeetingRatingRepository.save(meetingRating);
  }
}
