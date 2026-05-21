package com.ces.eos.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuarterResponse(
    UUID id,
    String name,
    String startDate,
    String endDate,
    Boolean isCurrent,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy) {}
