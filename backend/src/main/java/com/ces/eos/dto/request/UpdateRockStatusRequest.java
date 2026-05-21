package com.ces.eos.dto.request;

import com.ces.eos.annotation.ValueOfEnum;
import com.ces.eos.enums.RockStatus;
import jakarta.validation.constraints.NotBlank;

public record UpdateRockStatusRequest(
    @NotBlank(message = "status must not be blank") @ValueOfEnum(enumClass = RockStatus.class)
        String status) {}
