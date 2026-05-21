package com.ces.eos.controller;

import com.ces.eos.dto.request.CreateRockRequest;
import com.ces.eos.dto.request.UpdateRockArchiveRequest;
import com.ces.eos.dto.request.UpdateRockRequest;
import com.ces.eos.dto.request.UpdateRockStatusRequest;
import com.ces.eos.dto.response.RockListResponse;
import com.ces.eos.dto.response.RockResponse;
import com.ces.eos.dto.response.UserRockListResponse;
import com.ces.eos.enums.RockStatus;
import com.ces.eos.security.CustomUserDetails;
import com.ces.eos.service.RockService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rocks")
@RequiredArgsConstructor
public class RockController {

  private final RockService rockService;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeam(#teamId)")
  public ResponseEntity<RockListResponse> getRocksByTeam(
      @RequestParam UUID teamId,
      @RequestParam UUID yearId,
      @RequestParam UUID quarterId,
      @RequestParam(defaultValue = "false") Boolean isArchived) {
    RockListResponse response = rockService.getRocksByTeam(teamId, yearId, quarterId, isArchived);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeam(#request.teamId)")
  public ResponseEntity<RockResponse> addRock(
      @Valid @RequestBody CreateRockRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    RockResponse response = rockService.addRock(request, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping("/{rockId}")
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeamByRockId(#rockId)")
  public ResponseEntity<RockResponse> updateRockArchiveStatus(
      @PathVariable UUID rockId, @Valid @RequestBody UpdateRockArchiveRequest request) {
    RockResponse response = rockService.updateRockArchiveStatus(rockId, request.isArchived());
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{rockId}/status")
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeamByRockId(#rockId)")
  public ResponseEntity<RockResponse> updateRockStatus(
      @PathVariable UUID rockId, @Valid @RequestBody UpdateRockStatusRequest request) {
    RockResponse response =
        rockService.updateRockStatus(rockId, RockStatus.valueOf(request.status()));
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{rockId}")
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeamByRockId(#rockId)")
  public ResponseEntity<RockResponse> updateRock(
      @PathVariable UUID rockId,
      @Valid @RequestBody UpdateRockRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    RockResponse response = rockService.updateRock(rockId, request, userDetails.getId());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/me")
  public ResponseEntity<UserRockListResponse> getRocksAssignedToCurrentUser(
      @AuthenticationPrincipal CustomUserDetails currentUser,
      @RequestParam UUID yearId,
      @RequestParam UUID quarterId) {
    UserRockListResponse response =
        rockService.findActiveRocksByOwnerId(currentUser.getId(), yearId, quarterId);
    return ResponseEntity.ok(response);
  }
}
