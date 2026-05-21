package com.ces.eos.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TrendsTabMetricResponse(
    UUID id,
    String name,
    String goal,
    String unit,
    String operator,
    List<TrendDataPointResponse> values,
    TeamBaseResponse team,
    UserBaseResponse owner,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy) {}
