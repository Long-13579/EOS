package com.ces.eos.controller;

import com.ces.eos.dto.request.CreateTeamRequest;
import com.ces.eos.dto.request.GetTeamsRequest;
import com.ces.eos.dto.request.UpdateTeamRequest;
import com.ces.eos.dto.response.PagedEntityResponse;
import com.ces.eos.dto.response.TeamResponse;
import com.ces.eos.dto.response.UserBaseResponse;
import com.ces.eos.service.TeamService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

  private final TeamService teamService;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PagedEntityResponse<TeamResponse>> getTeams(
      @Valid @ModelAttribute GetTeamsRequest request) {
    PagedEntityResponse<TeamResponse> response = teamService.getTeamsWithPagination(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<TeamResponse> addTeam(@Valid @RequestBody CreateTeamRequest request) {
    TeamResponse response = teamService.addTeam(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{team-id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<TeamResponse> updateTeam(
      @PathVariable("team-id") UUID teamId, @Valid @RequestBody UpdateTeamRequest request) {
    TeamResponse response = teamService.updateTeam(teamId, request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{team-id}/users")
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeam(#teamId)")
  public ResponseEntity<List<UserBaseResponse>> getUsersByTeamId(
      @PathVariable("team-id") UUID teamId) {
    List<UserBaseResponse> response = teamService.getUsersByTeamId(teamId);
    return ResponseEntity.ok(response);
  }
}
