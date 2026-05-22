package com.ces.eos.dto.request;

import com.ces.eos.annotation.ValueOfEnum;
import com.ces.eos.enums.L10MeetingRatingValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record L10MeetingRatingItemRequest(
    @NotNull(message = "Member ID cannot be null") UUID memberId,
    @NotBlank(message = "Rating cannot be blank")
        @ValueOfEnum(
            enumClass = L10MeetingRatingValue.class,
            message = "Rating must be one of {acceptedValues}")
        String rating) {}
