package com.ces.eos.controller;

import com.ces.eos.dto.request.CreateMetricRequest;
import com.ces.eos.dto.request.UpdateMetricRequest;
import com.ces.eos.dto.response.MetricResponse;
import com.ces.eos.dto.response.TrendsTabMetricListResponse;
import com.ces.eos.security.CustomUserDetails;
import com.ces.eos.service.MetricService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricController {
  private final MetricService metricService;

  @PostMapping
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeam(#request.teamId)")
  public ResponseEntity<MetricResponse> addMetric(
      @Valid @RequestBody CreateMetricRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    MetricResponse response = metricService.addMetric(request, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeam(#teamId)")
  public ResponseEntity<List<MetricResponse>> listMetricsByTeamAndWeek(
      @RequestParam UUID teamId, @RequestParam UUID weekId) {
    List<MetricResponse> response = metricService.listMetricsByTeamAndWeek(teamId, weekId);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{metricId}")
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeamByMetricId(#metricId)")
  public ResponseEntity<MetricResponse> updateMetric(
      @PathVariable UUID metricId,
      @Valid @RequestBody UpdateMetricRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    MetricResponse response = metricService.updateMetric(metricId, request, userDetails.getId());
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{metricId}")
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeamByMetricId(#metricId)")
  public ResponseEntity<Void> deleteMetricById(@PathVariable UUID metricId) {
    metricService.deleteMetricById(metricId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/trends")
  @PreAuthorize("hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeam(#teamId)")
  public ResponseEntity<TrendsTabMetricListResponse> listTrendsTabMetricsByTeam(
      @RequestParam UUID teamId) {
    TrendsTabMetricListResponse response = metricService.listTrendsTabMetricsByTeam(teamId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/me")
  public ResponseEntity<List<MetricResponse>> listMyAssignedMetrics(
      @RequestParam UUID weekId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<MetricResponse> response =
        metricService.listMyAssignedMetrics(userDetails.getId(), weekId);
    return ResponseEntity.ok(response);
  }
}
