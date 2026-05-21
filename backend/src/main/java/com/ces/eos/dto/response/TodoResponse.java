package com.ces.eos.dto.response;

import com.ces.eos.enums.TodoStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TodoResponse(
    UUID id,
    String title,
    String description,
    TodoStatus status,
    Instant dueDate,
    Boolean isArchived,
    Instant createdAt,
    Instant updatedAt,
    UUID createdBy,
    UUID updatedBy,
    List<UserBaseResponse> assignees,
    TeamBaseResponse team) {}
