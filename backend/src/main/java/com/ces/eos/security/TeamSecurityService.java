package com.ces.eos.security;

import com.ces.eos.repository.HeadlineRepository;
import com.ces.eos.repository.IssueRepository;
import com.ces.eos.repository.MetricRepository;
import com.ces.eos.repository.RockRepository;
import com.ces.eos.repository.TodoRepository;
import com.ces.eos.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamSecurityService {

  private final UserRepository userRepository;
  private final IssueRepository issueRepository;
  private final TodoRepository todoRepository;
  private final HeadlineRepository headlineRepository;
  private final RockRepository rockRepository;
  private final MetricRepository metricRepository;

  public boolean isCurrentUserMemberOfTeam(UUID teamId) {
    return isUserMemberOfTeam(getCurrentUserId(), teamId);
  }

  public boolean isUserMemberOfTeam(UUID userId, UUID teamId) {
    if (userId == null || teamId == null) {
      return false;
    }

    return userRepository.existsByIdAndTeams_Id(userId, teamId);
  }

  public boolean isCurrentUserMemberOfIssueTeam(UUID issueId) {
    UUID userId = getCurrentUserId();
    if (userId == null || issueId == null) {
      return false;
    }

    return issueRepository.existsByIdAndTeam_Users_Id(issueId, userId);
  }

  public boolean isCurrentUserMemberOfTeamByTodoId(UUID todoId) {
    UUID userId = getCurrentUserId();
    if (userId == null || todoId == null) {
      return false;
    }

    return todoRepository.existsByIdAndTeam_Users_Id(todoId, userId);
  }

  public boolean isCurrentUserMemberOfTeamByHeadlineId(UUID headlineId) {
    UUID userId = getCurrentUserId();
    if (userId == null || headlineId == null) {
      return false;
    }

    return headlineRepository.existsByIdAndTeam_Users_Id(headlineId, userId);
  }

  public boolean isCurrentUserMemberOfTeamByRockId(UUID rockId) {
    UUID userId = getCurrentUserId();
    if (userId == null || rockId == null) {
      return false;
    }

    return rockRepository.existsByIdAndTeam_Users_Id(rockId, userId);
  }

  public boolean isCurrentUserMemberOfTeamByMetricId(UUID metricId) {
    UUID userId = getCurrentUserId();
    if (userId == null || metricId == null) {
      return false;
    }

    return metricRepository.existsByIdAndTeam_Users_Id(metricId, userId);
  }

  private UUID getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof CustomUserDetails customUserDetails) {
      return customUserDetails.getId();
    }

    return null;
  }
}
