package com.ces.eos.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
    UUID id,
    String firstName,
    String lastName,
    String email,
    String role,
    Boolean isActive,
    Instant createdAt,
    Instant updatedAt,
    UUID createdBy,
    UUID updatedBy,
    Set<TeamBaseResponse> teams) {}
