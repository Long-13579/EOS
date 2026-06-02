package com.ces.eos.service.impl;

import com.ces.eos.dto.response.L10MeetingChangeLogResponse;
import com.ces.eos.entity.L10Meeting;
import com.ces.eos.entity.L10MeetingChangeLog;
import com.ces.eos.entity.Team;
import com.ces.eos.mapper.L10MeetingChangeLogMapper;
import com.ces.eos.repository.L10MeetingChangeLogRepository;
import com.ces.eos.repository.L10MeetingRepository;
import com.ces.eos.repository.TeamRepository;
import com.ces.eos.service.L10MeetingChangeLogService;
import com.ces.eos.util.DateUtils;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class L10MeetingChangeLogServiceImpl implements L10MeetingChangeLogService {

  private final L10MeetingChangeLogRepository changeLogRepository;
  private final L10MeetingRepository meetingRepository;
  private final TeamRepository teamRepository;
  private final L10MeetingChangeLogMapper changeLogMapper;

  @Override
  @Transactional
  public void logChange(
      UUID teamId,
      String entityType,
      UUID entityId,
      String beforeSnapshot,
      String afterSnapshot) {
    log.debug(
        "action=logChange.start teamId={} entityType={} entityId={}",
        teamId,
        entityType,
        entityId);

    // Find active meeting for the team
    Optional<UUID> activeMeetingId = findActiveMeetingId(teamId);
    if (activeMeetingId.isEmpty()) {
      log.debug(
          "action=logChange.noActiveMeeting teamId={} entityType={} entityId={} - skipping",
          teamId,
          entityType,
          entityId);
      return;
    }

    // Skip entries with no net change
    if (beforeSnapshot != null && afterSnapshot != null && beforeSnapshot.equals(afterSnapshot)) {
      log.debug(
          "action=logChange.noNetChange teamId={} entityType={} entityId={} - skipping",
          teamId,
          entityType,
          entityId);
      return;
    }

    L10Meeting meeting = new L10Meeting();
    meeting.setId(activeMeetingId.get());

    L10MeetingChangeLog changeLog =
        L10MeetingChangeLog.builder()
            .meeting(meeting)
            .entityType(entityType)
            .entityId(entityId)
            .beforeSnapshot(beforeSnapshot)
            .afterSnapshot(afterSnapshot)
            .build();

    changeLogRepository.save(changeLog);
    log.debug(
        "action=logChange.success teamId={} entityType={} entityId={} meetingId={}",
        teamId,
        entityType,
        entityId,
        activeMeetingId.get());
  }

  @Override
  public List<L10MeetingChangeLogResponse> getChangeLogsByMeetingId(UUID meetingId) {
    log.debug("action=getChangeLogsByMeetingId.start meetingId={}", meetingId);
    List<L10MeetingChangeLog> logs = changeLogRepository.findByMeetingIdOrderedByCreatedAt(meetingId);
    return logs.stream().map(changeLogMapper::toL10MeetingChangeLogResponse).toList();
  }

  @Override
  public Optional<UUID> findActiveMeetingId(UUID teamId) {
    log.debug("action=findActiveMeetingId.start teamId={}", teamId);

    // Get the team to access its timezone
    Optional<Team> team = teamRepository.findById(teamId);
    if (team.isEmpty()) {
      log.debug("action=findActiveMeetingId.teamNotFound teamId={}", teamId);
      return Optional.empty();
    }

    // Get today's date in the team's timezone
    LocalDate today = DateUtils.getTodayForTimezone(team.get().getTimezone());
    
    Optional<L10Meeting> activeMeeting = meetingRepository.findActiveMeetingByTeamAndDate(teamId, today);
    if (activeMeeting.isPresent()) {
      log.debug(
          "action=findActiveMeetingId.found teamId={} meetingId={}", teamId, activeMeeting.get().getId());
      return Optional.of(activeMeeting.get().getId());
    }

    log.debug("action=findActiveMeetingId.notFound teamId={}", teamId);
    return Optional.empty();
  }
}
