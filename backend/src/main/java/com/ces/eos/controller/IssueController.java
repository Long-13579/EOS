package com.ces.eos.controller;

import com.ces.eos.dto.request.CreateIssueRequest;
import com.ces.eos.dto.request.PaginationRequest;
import com.ces.eos.dto.request.UpdateIssueArchiveRequest;
import com.ces.eos.dto.request.UpdateIssueRequest;
import com.ces.eos.dto.request.UpdateIssueTypeRequest;
import com.ces.eos.dto.response.IssueResponse;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.security.CustomUserDetails;
import com.ces.eos.service.IssueService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/issues")
@RequiredArgsConstructor
public class IssueController {

  private final IssueService issueService;

  @PostMapping
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeam(#request.teamId)")
  public ResponseEntity<IssueResponse> addIssue(
      @Valid @RequestBody CreateIssueRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    IssueResponse response = issueService.addIssue(request, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeam(#teamId)")
  public ResponseEntity<PagedEntityResponse<IssueResponse>> getIssuesByTeam(
      @Valid @ModelAttribute PaginationRequest request,
      @RequestParam UUID teamId,
      @RequestParam(required = false) String issueTypeId,
      @RequestParam(defaultValue = "false") Boolean isArchived) {
    PagedEntityResponse<IssueResponse> response =
        issueService.getIssuesByTeam(teamId, request, issueTypeId, isArchived);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{issueId}")
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfIssueTeam(#issueId)")
  public ResponseEntity<Void> deleteIssueById(@PathVariable UUID issueId) {
    issueService.deleteIssueById(issueId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{issueId}")
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfIssueTeam(#issueId)")
  public ResponseEntity<IssueResponse> updateIssue(
      @PathVariable UUID issueId, @Valid @RequestBody UpdateIssueRequest request) {
    IssueResponse response = issueService.updateIssue(issueId, request);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{issueId}")
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfIssueTeam(#issueId)")
  public ResponseEntity<IssueResponse> updateIssueArchiveStatus(
      @PathVariable UUID issueId, @Valid @RequestBody UpdateIssueArchiveRequest request) {
    IssueResponse response = issueService.updateIssueArchiveStatus(issueId, request.isArchived());
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{issueId}/issue-type")
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfIssueTeam(#issueId)")
  public ResponseEntity<IssueResponse> updateIssueType(
      @PathVariable UUID issueId, @Valid @RequestBody UpdateIssueTypeRequest request) {
    IssueResponse response = issueService.updateIssueType(issueId, request.issueTypeId());
    return ResponseEntity.ok(response);
  }
}
