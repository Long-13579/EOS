package com.ces.eos.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateMetricArchiveRequest(
    @NotNull(message = "isArchived must not be null") Boolean isArchived) {}
