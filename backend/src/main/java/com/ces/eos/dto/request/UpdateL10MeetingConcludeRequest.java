package com.ces.eos.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateL10MeetingConcludeRequest(
    @NotBlank(message = "Key decisions cannot be blank") String keyDecisions,
    @NotBlank(message = "Cascading message cannot be blank") String cascadingMessage) {}
