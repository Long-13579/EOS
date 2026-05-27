package com.ces.eos.dto.response;

import com.ces.eos.enums.L10MeetingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record L10MeetingResponse(
    UUID id,
    TeamBaseResponse team,
    LocalDate meetingDate,
    LocalTime meetingTime,
    LocalDate weekStartDate,
    UserBaseResponse facilitator,
    UserBaseResponse scribe,
    L10MeetingStatus status,
    String concludeKeyDecisions,
    String concludeCascadingMessage,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy,
    List<L10MeetingRatingResponse> ratings) {}
