package com.ces.eos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTeamRequest(
    @NotBlank(message = "must not be blank")
        @Size(max = 100, message = "must be between 1 and 100 characters")
        @Pattern(
            regexp = "^[a-zA-ZÀ-ỹ0-9]+(?:[ \\-'][a-zA-ZÀ-ỹ0-9]+)*$",
            message =
                "must not contain leading/trailing whitespace and can only contain letters, numbers, spaces, hyphens, and apostrophes")
        String name) {}
