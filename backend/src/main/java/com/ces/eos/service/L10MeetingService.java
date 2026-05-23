package com.ces.eos.service;

import com.ces.eos.dto.request.CreateL10MeetingRequest;
import com.ces.eos.dto.request.PaginationRequest;
import com.ces.eos.dto.request.UpdateL10MeetingConcludeRequest;
import com.ces.eos.dto.request.UpsertL10MeetingRatingsRequest;
import com.ces.eos.dto.response.L10MeetingRatingResponse;
import com.ces.eos.dto.response.L10MeetingResponse;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.enums.L10MeetingStatus;
import java.util.List;
import java.util.UUID;

public interface L10MeetingService {
  L10MeetingResponse scheduleMeeting(CreateL10MeetingRequest request, UUID schedulerId);

  L10MeetingResponse startMeeting(UUID meetingId, UUID userId);

  L10MeetingResponse updateConclude(
      UUID meetingId, UpdateL10MeetingConcludeRequest request, UUID userId);

  List<L10MeetingRatingResponse> upsertRatings(
      UUID meetingId, UUID userId, UpsertL10MeetingRatingsRequest request);

  PagedEntityResponse<L10MeetingResponse> getMeetingsByTeam(
      UUID teamId, L10MeetingStatus status, PaginationRequest request);
}
