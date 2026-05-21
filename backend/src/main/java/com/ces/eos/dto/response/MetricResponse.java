package com.ces.eos.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MetricResponse(
    UUID id,
    String name,
    String goal,
    String unit,
    String operator,
    String lastValue,
    MetricValueResponse currentValue,
    TeamBaseResponse team,
    UserBaseResponse owner,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy) {}
