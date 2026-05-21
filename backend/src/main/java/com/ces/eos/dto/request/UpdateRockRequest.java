package com.ces.eos.dto.request;

import com.ces.eos.annotation.ValueOfEnum;
import com.ces.eos.enums.RockCategory;
import com.ces.eos.enums.RockStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record UpdateRockRequest(
    @NotBlank(message = "Title cannot be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,
    @NotBlank(message = "Description cannot be blank")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,
    @NotBlank(message = "Status cannot be blank") @ValueOfEnum(enumClass = RockStatus.class)
        String status,
    @NotBlank(message = "Category cannot be blank") @ValueOfEnum(enumClass = RockCategory.class)
        String category,
    @NotNull(message = "Due date cannot be null") Instant dueDate,
    @NotNull(message = "Year cannot be null")
        @Min(value = 1900, message = "Year must be greater than or equal to 1900")
        Integer year,
    @NotNull(message = "Quarter ID cannot be null") UUID quarterId,
    @NotNull(message = "Owner ID cannot be null") UUID ownerId) {}
