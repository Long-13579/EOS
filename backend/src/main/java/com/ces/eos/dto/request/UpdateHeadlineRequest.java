package com.ces.eos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateHeadlineRequest(
    @NotBlank(message = "Title cannot be blank")
        @Size(max = 2000, message = "Title must not exceed 2000 characters")
        String title) {}
