package com.ces.eos.controller;

import com.ces.eos.dto.request.CreateHeadlineRequest;
import com.ces.eos.dto.request.PaginationRequest;
import com.ces.eos.dto.request.UpdateHeadlineArchiveRequest;
import com.ces.eos.dto.request.UpdateHeadlineRequest;
import com.ces.eos.dto.response.HeadlineResponse;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.security.CustomUserDetails;
import com.ces.eos.service.HeadlineService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/headlines")
@RequiredArgsConstructor
public class HeadlineController {

  private final HeadlineService headlineService;

  @PostMapping
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeam(#request.teamId)")
  public ResponseEntity<HeadlineResponse> createHeadline(
      @Valid @RequestBody CreateHeadlineRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    HeadlineResponse response = headlineService.createHeadline(request, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeam(#teamId)")
  public ResponseEntity<PagedEntityResponse<HeadlineResponse>> getHeadlinesByTeam(
      @Valid @ModelAttribute PaginationRequest request,
      @RequestParam UUID teamId,
      @RequestParam(defaultValue = "false") Boolean isArchived) {
    PagedEntityResponse<HeadlineResponse> response =
        headlineService.getHeadlinesByTeam(teamId, request, isArchived);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{headlineId}")
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeamByHeadlineId(#headlineId)")
  public ResponseEntity<HeadlineResponse> updateHeadline(
      @PathVariable UUID headlineId,
      @Valid @RequestBody UpdateHeadlineRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    HeadlineResponse response =
        headlineService.updateHeadline(headlineId, request, userDetails.getId());
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{headlineId}")
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeamByHeadlineId(#headlineId)")
  public ResponseEntity<Void> deleteHeadline(@PathVariable UUID headlineId) {
    headlineService.deleteHeadline(headlineId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{headlineId}")
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeamByHeadlineId(#headlineId)")
  public ResponseEntity<HeadlineResponse> updateHeadlineArchiveStatus(
      @PathVariable UUID headlineId, @Valid @RequestBody UpdateHeadlineArchiveRequest request) {
    HeadlineResponse response =
        headlineService.updateHeadlineArchiveStatus(headlineId, request.isArchived());
    return ResponseEntity.ok(response);
  }
}
