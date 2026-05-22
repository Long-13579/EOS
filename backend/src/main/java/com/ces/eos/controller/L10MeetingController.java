package com.ces.eos.controller;

import com.ces.eos.dto.request.CreateL10MeetingRequest;
import com.ces.eos.dto.request.UpdateL10MeetingConcludeRequest;
import com.ces.eos.dto.request.UpsertL10MeetingRatingsRequest;
import com.ces.eos.dto.response.L10MeetingRatingResponse;
import com.ces.eos.dto.response.L10MeetingResponse;
import com.ces.eos.security.CustomUserDetails;
import com.ces.eos.service.L10MeetingService;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/l10-meetings")
@RequiredArgsConstructor
public class L10MeetingController {

  private final L10MeetingService l10MeetingService;

  @PostMapping
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeam(#request.teamId)")
  public ResponseEntity<L10MeetingResponse> scheduleMeeting(
      @Valid @RequestBody CreateL10MeetingRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    L10MeetingResponse response = l10MeetingService.scheduleMeeting(request, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/{meetingId}/start")
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfL10Meeting(#meetingId)")
  public ResponseEntity<L10MeetingResponse> startMeeting(
      @PathVariable UUID meetingId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    L10MeetingResponse response = l10MeetingService.startMeeting(meetingId, userDetails.getId());
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{meetingId}/conclude")
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfL10Meeting(#meetingId)")
  public ResponseEntity<L10MeetingResponse> updateConclude(
      @PathVariable UUID meetingId,
      @Valid @RequestBody UpdateL10MeetingConcludeRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    L10MeetingResponse response =
        l10MeetingService.updateConclude(meetingId, request, userDetails.getId());
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{meetingId}/ratings")
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfL10Meeting(#meetingId)")
  public ResponseEntity<List<L10MeetingRatingResponse>> upsertRatings(
      @PathVariable UUID meetingId,
      @Valid @RequestBody UpsertL10MeetingRatingsRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<L10MeetingRatingResponse> response =
        l10MeetingService.upsertRatings(meetingId, userDetails.getId(), request);
    return ResponseEntity.ok(response);
  }
}
