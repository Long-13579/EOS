package com.ces.eos.dto.request;

import com.ces.eos.annotation.ValueOfEnum;
import com.ces.eos.enums.TodoStatus;
import jakarta.validation.constraints.NotBlank;

public record UpdateTodoStatusRequest(
    @NotBlank(message = "status must not be blank") @ValueOfEnum(enumClass = TodoStatus.class)
        String status) {}
