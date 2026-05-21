package com.ces.eos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateHeadlineRequest(
    @NotBlank(message = "Title cannot be blank")
        @Size(max = 2000, message = "Title must not exceed 2000 characters")
        String title,
    @NotNull(message = "Team ID cannot be null") UUID teamId) {}
