package com.ces.eos.controller;

import com.ces.eos.dto.request.UpdateMetricValueRequest;
import com.ces.eos.dto.response.MetricResponse;
import com.ces.eos.security.CustomUserDetails;
import com.ces.eos.service.MetricValueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metric-values")
@RequiredArgsConstructor
public class MetricValueController {

  private final MetricValueService metricValueService;

  @PutMapping
  @PreAuthorize(
      "hasRole('ADMIN') or @teamSecurityService.isCurrentUserMemberOfTeamByMetricId(#request.metricId)")
  public ResponseEntity<MetricResponse> updateMetricValue(
      @Valid @RequestBody UpdateMetricValueRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    MetricResponse response =
        metricValueService.updateMetricValue(
            request.metricId(), request.value(), userDetails.getId());

    return ResponseEntity.ok(response);
  }
}
