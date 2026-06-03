package com.ces.eos.controller;

import com.ces.eos.dto.request.CreateL10MeetingRequest;
import com.ces.eos.dto.request.PaginationRequest;
import com.ces.eos.dto.request.UpdateL10MeetingConcludeRequest;
import com.ces.eos.dto.request.UpdateL10MeetingRequest;
import com.ces.eos.dto.request.UpsertL10MeetingRatingsRequest;
import com.ces.eos.dto.response.L10MeetingRatingResponse;
import com.ces.eos.dto.response.L10MeetingResponse;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.enums.L10MeetingStatus;
import com.ces.eos.security.CustomUserDetails;
import com.ces.eos.service.L10MeetingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/l10-meetings")
@RequiredArgsConstructor
public class L10MeetingController {

  private final L10MeetingService l10MeetingService;

  @GetMapping
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeam(#teamId)")
  public ResponseEntity<PagedEntityResponse<L10MeetingResponse>> getMeetingsByTeam(
      @Valid @ModelAttribute PaginationRequest request,
      @RequestParam UUID teamId,
      @RequestParam List<L10MeetingStatus> statuses) {
    PagedEntityResponse<L10MeetingResponse> response =
        l10MeetingService.getMeetingsByTeam(teamId, statuses, request);
    return ResponseEntity.ok(response);
  }

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

  @GetMapping("/{meetingId}")
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfL10Meeting(#meetingId)")
  public ResponseEntity<L10MeetingResponse> getMeeting(
      @PathVariable UUID meetingId) {
    L10MeetingResponse response = l10MeetingService.getMeeting(meetingId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{meetingId}/finish")
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfL10Meeting(#meetingId)")
  public ResponseEntity<L10MeetingResponse> finishMeeting(
      @PathVariable UUID meetingId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    L10MeetingResponse response = l10MeetingService.finishMeeting(meetingId, userDetails.getId());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{meetingId}/regenerate-summary")
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfL10Meeting(#meetingId)")
  public ResponseEntity<L10MeetingResponse> regenerateSummary(
      @PathVariable UUID meetingId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    L10MeetingResponse response =
        l10MeetingService.regenerateSummary(meetingId, userDetails.getId());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{meetingId}/ratings")
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfL10Meeting(#meetingId)")
  public ResponseEntity<List<L10MeetingRatingResponse>> getRatings(
      @PathVariable UUID meetingId) {
    List<L10MeetingRatingResponse> response = l10MeetingService.getRatings(meetingId);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{meetingId}")
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfL10Meeting(#meetingId)")
  public ResponseEntity<L10MeetingResponse> updateMeeting(
      @PathVariable UUID meetingId,
      @Valid @RequestBody UpdateL10MeetingRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    L10MeetingResponse response =
        l10MeetingService.updateMeeting(meetingId, request, userDetails.getId());
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{meetingId}")
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfL10Meeting(#meetingId)")
  public ResponseEntity<Void> deleteMeeting(
      @PathVariable UUID meetingId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    l10MeetingService.deleteMeeting(meetingId, userDetails.getId());
    return ResponseEntity.noContent().build();
  }
}
