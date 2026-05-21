package com.ces.eos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateIssueRequest(
    @NotBlank(message = "Title cannot be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,
    @Size(max = 2000, message = "Description must not exceed 2000 characters") String description,
    UUID issueTypeId,
    @NotNull(message = "Team ID cannot be null") UUID teamId) {}
