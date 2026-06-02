package com.ces.eos.service;

import com.ces.eos.dto.response.L10MeetingChangeLogResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface L10MeetingChangeLogService {

  /**
   * Log a change to an entity during an active meeting. If there's no active meeting for the team
   * on the current date, this is a no-op.
   *
   * @param teamId the team ID
   * @param entityType the type of entity being changed (e.g., "ISSUE", "ROCK")
   * @param entityId the ID of the entity being changed
   * @param beforeSnapshot the state of the entity before the change
   * @param afterSnapshot the state of the entity after the change
   */
  void logChange(
      UUID teamId,
      String entityType,
      UUID entityId,
      String beforeSnapshot,
      String afterSnapshot);

  /**
   * Get all change logs for a specific meeting.
   *
   * @param meetingId the meeting ID
   * @return list of change logs
   */
  List<L10MeetingChangeLogResponse> getChangeLogsByMeetingId(UUID meetingId);

  /**
   * Find the active meeting for a team on the current date (team timezone).
   *
   * @param teamId the team ID
   * @return the active meeting if found
   */
  Optional<UUID> findActiveMeetingId(UUID teamId);
}
