package com.ces.eos.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpsertL10MeetingRatingsRequest(
    @NotEmpty(message = "Ratings cannot be empty") @Valid List<L10MeetingRatingItemRequest> ratings) {}
