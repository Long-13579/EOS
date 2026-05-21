package com.ces.eos.service;

import com.ces.eos.dto.request.CreateMetricRequest;
import com.ces.eos.dto.request.UpdateMetricRequest;
import com.ces.eos.dto.response.MetricResponse;
import com.ces.eos.dto.response.TrendsTabMetricListResponse;
import com.ces.eos.entity.Metric;
import java.util.List;
import java.util.UUID;

public interface MetricService {
  MetricResponse addMetric(CreateMetricRequest request, UUID creatorId);

  List<MetricResponse> listMetricsByTeamAndWeek(UUID teamId, UUID weekId);

  MetricResponse updateMetric(UUID metricId, UpdateMetricRequest request, UUID updaterId);

  Metric getMetricById(UUID metricId);

  TrendsTabMetricListResponse listTrendsTabMetricsByTeam(UUID teamId);

  List<MetricResponse> listMyAssignedMetrics(UUID userId, UUID weekId);
}
