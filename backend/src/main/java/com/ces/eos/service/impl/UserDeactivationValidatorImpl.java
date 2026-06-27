package com.ces.eos.service.impl;

import com.ces.eos.enums.L10MeetingStatus;
import com.ces.eos.exception.ConflictException;
import com.ces.eos.repository.L10MeetingRepository;
import com.ces.eos.repository.MetricRepository;
import com.ces.eos.repository.RockRepository;
import com.ces.eos.repository.TodoRepository;
import com.ces.eos.service.UserDeactivationValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDeactivationValidatorImpl implements UserDeactivationValidator {

  private final RockRepository rockRepository;
  private final MetricRepository metricRepository;
  private final TodoRepository todoRepository;
  private final L10MeetingRepository l10MeetingRepository;

  @Override
  public void validate(UUID userId) {
    log.debug("action=validateUserDeactivation.start userId={}", userId);

    List<String> details = new ArrayList<>();

    List<String> rockTitles = rockRepository.findActiveTitlesByOwnerId(userId);
    if (!rockTitles.isEmpty()) {
      String message = "User owns active Rock(s): " + String.join(", ", rockTitles);
      details.add(message);
      log.debug(
          "action=validateUserDeactivation.rockBlocking userId={} count={}",
          userId, rockTitles.size());
    }

    List<String> metricNames = metricRepository.findActiveNamesByOwnerId(userId);
    if (!metricNames.isEmpty()) {
      String message = "User owns active Metric(s): " + String.join(", ", metricNames);
      details.add(message);
      log.debug(
          "action=validateUserDeactivation.metricBlocking userId={} count={}",
          userId, metricNames.size());
    }

    List<String> todoTitles = todoRepository.findActiveTitlesByAssigneeId(userId);
    if (!todoTitles.isEmpty()) {
      String message = "User is assignee of active Todo(s): " + String.join(", ", todoTitles);
      details.add(message);
      log.debug(
          "action=validateUserDeactivation.todoBlocking userId={} count={}",
          userId, todoTitles.size());
    }

    List<L10MeetingStatus> nonFinishedStatuses =
        List.of(L10MeetingStatus.SCHEDULED, L10MeetingStatus.STARTED);

    List<String> facilitatorMeetings =
        l10MeetingRepository.findUpcomingDatesByFacilitatorId(userId, nonFinishedStatuses);
    if (!facilitatorMeetings.isEmpty()) {
      String message =
          "User is facilitator for upcoming meeting(s): " + String.join(", ", facilitatorMeetings);
      details.add(message);
      log.debug(
          "action=validateUserDeactivation.facilitatorBlocking userId={} count={}",
          userId, facilitatorMeetings.size());
    }

    List<String> scribeMeetings =
        l10MeetingRepository.findUpcomingDatesByScribeId(userId, nonFinishedStatuses);
    if (!scribeMeetings.isEmpty()) {
      String message =
          "User is scribe for upcoming meeting(s): " + String.join(", ", scribeMeetings);
      details.add(message);
      log.debug(
          "action=validateUserDeactivation.scribeBlocking userId={} count={}",
          userId, scribeMeetings.size());
    }

    if (!details.isEmpty()) {
      log.warn(
          "action=validateUserDeactivation.validationFailed userId={} reasons={}",
          userId, details);
      throw new ConflictException(
          "Cannot deactivate user. Reassign responsibilities first.",
          Map.of("deactivation", details));
    }

    log.debug("action=validateUserDeactivation.success userId={}", userId);
  }
}
