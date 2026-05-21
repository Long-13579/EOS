package com.ces.eos.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HeadlineResponse(
    UUID id,
    String title,
    Boolean isArchived,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy,
    TeamBaseResponse team) {}
