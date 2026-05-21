package com.ces.eos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateMetricValueRequest(
    @NotNull(message = "Metric ID cannot be null") UUID metricId,
    @NotBlank(message = "Value cannot be blank")
        @Size(max = 255, message = "Value must not exceed 255 characters")
        String value) {}
