package com.ces.eos.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MetricValueResponse(
    UUID id,
    String value,
    Boolean isGoalMet,
    WeekResponse week,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse updatedBy) {}
