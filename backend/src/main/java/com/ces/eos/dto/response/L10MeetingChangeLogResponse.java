package com.ces.eos.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record L10MeetingChangeLogResponse(
    UUID id,
    UUID meetingId,
    String entityType,
    UUID entityId,
    String beforeSnapshot,
    String afterSnapshot,
    Instant createdAt,
    Instant updatedAt) {}
