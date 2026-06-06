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

  List<MetricResponse> listMetricsByTeamAndWeek(UUID teamId, UUID weekId, Boolean showArchived);

  MetricResponse updateMetric(UUID metricId, UpdateMetricRequest request, UUID updaterId);

  MetricResponse updateMetricArchiveStatus(UUID metricId, Boolean isArchived);

  Metric getMetricById(UUID metricId);

  TrendsTabMetricListResponse listTrendsTabMetricsByTeam(UUID teamId);

  List<MetricResponse> listMyAssignedMetrics(UUID userId, UUID weekId);

  void deleteMetricById(UUID metricId);
}
