package com.ces.eos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UpdateTodoRequest(
    @NotBlank(message = "Title cannot be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,
    String description,
    @NotBlank(message = "Status cannot be blank") String status,
    Instant dueDate,
    Set<UUID> assigneeIds) {}
