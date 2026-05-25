package com.ces.eos.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record UpdateL10MeetingRequest(
    @NotNull(message = "Meeting date cannot be null") LocalDate meetingDate,
    @NotNull(message = "Meeting time cannot be null") LocalTime meetingTime,
    @NotNull(message = "Facilitator ID cannot be null") UUID facilitatorId,
    @NotNull(message = "Scribe ID cannot be null") UUID scribeId) {}
