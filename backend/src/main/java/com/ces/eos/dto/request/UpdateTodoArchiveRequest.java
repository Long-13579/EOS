package com.ces.eos.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateTodoArchiveRequest(
    @NotNull(message = "isArchived must not be null") Boolean isArchived) {}
