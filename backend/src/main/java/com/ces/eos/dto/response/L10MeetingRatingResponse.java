package com.ces.eos.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record L10MeetingRatingResponse(
    UUID id,
    UUID meetingId,
    UserBaseResponse member,
    String rating,
    Instant createdAt,
    Instant updatedAt) {}
