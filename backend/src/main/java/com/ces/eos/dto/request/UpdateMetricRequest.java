package com.ces.eos.dto.request;

import com.ces.eos.annotation.ValueOfEnum;
import com.ces.eos.enums.MetricOperator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateMetricRequest(
    @NotBlank(message = "Name must not be blank")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,
    @NotBlank(message = "Goal must not be blank") String goal,
    @Pattern(regexp = "^\\S+$", message = "Operator must not contain whitespace")
        @ValueOfEnum(
            enumClass = MetricOperator.class,
            message = "Operator must be one of {acceptedValues}")
        String operator,
    @NotNull(message = "Owner ID must not be null") UUID ownerId) {}
