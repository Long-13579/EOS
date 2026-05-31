package com.ces.eos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CreateTodoRequest(
    @NotBlank(message = "Title cannot be blank")
        @Size(max = 255, message = "Title must be between 1 and 255 characters")
        String title,
    String description,
    @NotBlank(message = "Status cannot be blank") String status,
    Instant dueDate,
    @NotNull(message = "Team ID cannot be null") UUID teamId,
    Set<UUID> assigneeIds,
    UUID issueId) {}
