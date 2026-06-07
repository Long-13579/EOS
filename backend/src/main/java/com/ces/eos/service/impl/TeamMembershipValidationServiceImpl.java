package com.ces.eos.service.impl;

import com.ces.eos.exception.ConflictException;
import com.ces.eos.repository.MetricRepository;
import com.ces.eos.repository.RockRepository;
import com.ces.eos.repository.TodoRepository;
import com.ces.eos.service.TeamMembershipValidationService;
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
public class TeamMembershipValidationServiceImpl implements TeamMembershipValidationService {

  private final RockRepository rockRepository;
  private final MetricRepository metricRepository;
  private final TodoRepository todoRepository;

  @Override
  public void validateUserTeamRemoval(UUID userId, UUID teamId) {
    log.debug("action=validateUserTeamRemoval.start userId={} teamId={}", userId, teamId);

    List<String> details = new ArrayList<>();

    List<String> rockTitles =
        rockRepository.findActiveTitlesByOwnerIdAndTeamId(userId, teamId);
    if (!rockTitles.isEmpty()) {
      String message =
          "User owns active Rock(s): " + String.join(", ", rockTitles);
      details.add(message);
      log.debug(
          "action=validateUserTeamRemoval.rockBlocking userId={} teamId={} count={}",
          userId, teamId, rockTitles.size());
    }

    List<String> metricNames =
        metricRepository.findActiveNamesByOwnerIdAndTeamId(userId, teamId);
    if (!metricNames.isEmpty()) {
      String message =
          "User owns active Metric(s): " + String.join(", ", metricNames);
      details.add(message);
      log.debug(
          "action=validateUserTeamRemoval.metricBlocking userId={} teamId={} count={}",
          userId, teamId, metricNames.size());
    }

    List<String> todoTitles =
        todoRepository.findActiveTitlesByAssigneeIdAndTeamId(userId, teamId);
    if (!todoTitles.isEmpty()) {
      String message =
          "User is assignee of active Todo(s): " + String.join(", ", todoTitles);
      details.add(message);
      log.debug(
          "action=validateUserTeamRemoval.todoBlocking userId={} teamId={} count={}",
          userId, teamId, todoTitles.size());
    }

    if (!details.isEmpty()) {
      log.warn(
          "action=validateUserTeamRemoval.validationFailed userId={} teamId={} reasons={}",
          userId, teamId, details);
      throw new ConflictException(
          "Cannot remove user from team. Reassign responsibilities first.",
          Map.of("teamRemoval", details));
    }

    log.debug("action=validateUserTeamRemoval.success userId={} teamId={}", userId, teamId);
  }
}
