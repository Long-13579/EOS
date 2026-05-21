package com.ces.eos.dto.request;

import com.ces.eos.annotation.ValueOfEnum;
import com.ces.eos.enums.MetricOperator;
import com.ces.eos.enums.MetricUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateMetricRequest(
    @NotBlank(message = "Name cannot be blank")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,
    @NotBlank(message = "Goal cannot be blank") String goal,
    @NotBlank(message = "Unit cannot be blank")
        @ValueOfEnum(enumClass = MetricUnit.class, message = "Unit must be one of {acceptedValues}")
        String unit,
    @Pattern(regexp = "^\\S+$", message = "Operator must not be blank")
        @ValueOfEnum(
            enumClass = MetricOperator.class,
            message = "Operator must be one of {acceptedValues}")
        String operator,
    @NotNull(message = "Team ID cannot be null") UUID teamId,
    @NotNull(message = "Owner ID cannot be null") UUID ownerId) {}
