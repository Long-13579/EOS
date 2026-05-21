package com.ces.eos.service;

import com.ces.eos.dto.response.MetricResponse;
import com.ces.eos.entity.Metric;
import com.ces.eos.entity.MetricValue;
import com.ces.eos.entity.Week;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MetricValueService {
  MetricValue addDefaultMetricValueForWeek(Metric metric, Week week);

  MetricResponse updateMetricValue(UUID metricId, String value, UUID updaterId);

  List<MetricValue> findByMetricIdsAndWeekIds(List<UUID> metricIds, List<UUID> weekIds);

  Map<UUID, Map<UUID, MetricValue>> groupByMetricIdAndWeekId(List<MetricValue> values);
}
