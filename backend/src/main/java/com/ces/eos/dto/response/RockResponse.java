package com.ces.eos.dto.response;

import com.ces.eos.enums.RockCategory;
import com.ces.eos.enums.RockStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RockResponse(
    UUID id,
    String title,
    String description,
    RockStatus status,
    RockCategory category,
    Boolean isArchived,
    Instant dueDate,
    UserBaseResponse owner,
    TeamBaseResponse team,
    YearBaseResponse year,
    QuarterBaseResponse quarter,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy) {}
