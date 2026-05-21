package com.ces.eos.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GetEntitiesRequest(
    @Min(value = 1, message = "Page number must be greater than or equal to 1") Integer page,
    @Min(value = 1, message = "Limit must be at least 1")
        @Max(value = 20, message = "Limit must not exceed 20")
        Integer limit,
    @Pattern(
            regexp = "^[a-zA-ZÀ-ỹ\\s@.\\-_0-9]*$",
            message =
                "Search field can only contain letters, numbers, spaces, and email characters (@, ., -, _)")
        @Size(max = 30, message = "Search field must not exceed 30 characters")
        String search) {
  public GetEntitiesRequest {
    if (page == null) page = 1;
    if (limit == null) limit = 10;
  }
}
