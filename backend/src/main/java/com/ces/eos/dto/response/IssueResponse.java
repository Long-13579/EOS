package com.ces.eos.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record IssueResponse(
    UUID id,
    String title,
    String description,
    IssueTypeBaseResponse issueType,
    Boolean isArchived,
    Instant createdAt,
    Instant updatedAt,
    UUID createdBy,
    UUID updatedBy,
    UserBaseResponse creator,
    TeamBaseResponse team) {}
