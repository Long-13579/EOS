package com.ces.eos.service.impl;

import com.ces.eos.dto.response.MetricResponse;
import com.ces.eos.entity.Metric;
import com.ces.eos.entity.MetricValue;
import com.ces.eos.entity.User;
import com.ces.eos.entity.Week;
import com.ces.eos.enums.MetricUnit;
import com.ces.eos.exception.BadRequestException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.exception.ServerInternalException;
import com.ces.eos.mapper.MetricMapper;
import com.ces.eos.repository.MetricRepository;
import com.ces.eos.repository.MetricValueRepository;
import com.ces.eos.service.MetricValueService;
import com.ces.eos.service.UserService;
import com.ces.eos.service.WeekService;
import com.ces.eos.util.MetricUtil;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetricValueServiceImpl implements MetricValueService {
  private final MetricValueRepository metricValueRepository;
  private final MetricMapper metricMapper;
  private final UserService userService;
  private final WeekService weekService;
  private final MetricRepository metricRepository;

  @Override
  @Transactional
  public MetricValue addDefaultMetricValueForWeek(Metric metric, Week week) {
    log.info("action=addDefaultMetricValueForWeek.start");
    log.debug("action=addDefaultMetricValueForWeek.repo.save");
    MetricValue metricValue =
        metricValueRepository.save(MetricValue.builder().metric(metric).week(week).build());
    log.info("action=addDefaultMetricValueForWeek.success metricValueId={}", metricValue.getId());
    return metricValue;
  }

  @Override
  @Transactional
  public MetricResponse updateMetricValue(UUID metricId, String value, UUID updaterId) {
    log.info("action=updateMetricValue.start metricId={} updaterId={}", metricId, updaterId);
    Metric metric = getMetricById(metricId);

    MetricValue metricValue = getOrCreateCurrentMetricValue(metricId);

    log.debug("action=updateMetricValue.service.getUserById updaterId={}", updaterId);
    User updater = userService.getUserById(updaterId);

    validateMetricValue(metric.getUnit(), value);

    metricValue.setValue(value);
    metricValue.setUpdatedBy(updater);

    log.debug("action=updateMetricValue.repo.save metricValueId={}", metricValue.getId());
    metricValueRepository.save(metricValue);

    Week currentWeek = metricValue.getWeek();

    MetricValue previousWeekMetricValue =
        getPreviousWeekMetricValue(currentWeek.getStartDate(), metric.getId());

    MetricResponse response =
        metricMapper.toMetricResponse(metric, metricValue, previousWeekMetricValue);
    log.info("action=updateMetricValue.success metricId={}", metricId);
    return response;
  }

  private void validateMetricValue(MetricUnit unit, String value) {
    try {
      MetricUtil.validateMetricUpdate(unit, value);
    } catch (IllegalArgumentException e) {
      log.warn("action=validateMetricValue.validationFailed unit={}", unit);
      throw new BadRequestException(Map.of("value", List.of(e.getMessage())));
    }
  }

  @Transactional
  private MetricValue getOrCreateCurrentMetricValue(UUID metricId) {
    log.debug("action=getOrCreateCurrentMetricValue.service.getOrCreateCurrentWeek");
    UUID currentWeekId = weekService.getOrCreateCurrentWeek().getId();
    log.debug(
        "action=getOrCreateCurrentMetricValue.repo.insertIfNotExists metricId={} weekId={}",
        metricId,
        currentWeekId);
    metricValueRepository.insertIfNotExists(metricId, currentWeekId);
    log.debug(
        "action=getOrCreateCurrentMetricValue.repo.findByMetricIdAndWeekId metricId={} weekId={}",
        metricId,
        currentWeekId);
    return metricValueRepository
        .findByMetricIdAndWeekId(metricId, currentWeekId)
        .orElseThrow(
            () ->
                new ServerInternalException(
                    String.format(
                        "Metric value not found for metricId=%s and weekId=%s after insert"
                            + " attempt.",
                        metricId, currentWeekId)));
  }

  private MetricValue getPreviousWeekMetricValue(LocalDate startDate, UUID metricId) {
    return weekService
        .getPreviousWeek(startDate)
        .flatMap(
            previousWeek ->
                metricValueRepository.findByMetricIdAndWeekId(metricId, previousWeek.getId()))
        .orElse(null);
  }

  private Metric getMetricById(UUID metricId) {
    return metricRepository
        .findByIdWithTeam(metricId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    Map.of(
                        "metricId",
                        List.of(String.format("Metric not found with id: %s", metricId)))));
  }

  @Override
  public List<MetricValue> findByMetricIdsAndWeekIds(List<UUID> metricIds, List<UUID> weekIds) {
    log.info(
        "action=findByMetricIdsAndWeekIds.start metricCount={} weekCount={}",
        metricIds == null ? 0 : metricIds.size(),
        weekIds == null ? 0 : weekIds.size());
    if (CollectionUtils.isEmpty(metricIds) || CollectionUtils.isEmpty(weekIds)) {
      log.debug("action=findByMetricIdsAndWeekIds.branch.emptyInput");
      log.info("action=findByMetricIdsAndWeekIds.success count=0");
      return Collections.emptyList();
    }

    log.debug(
        "action=findByMetricIdsAndWeekIds.repo.findByMetricIdsAndWeekIds metricCount={} weekCount={}",
        metricIds.size(),
        weekIds.size());
    List<MetricValue> values = metricValueRepository.findByMetricIdsAndWeekIds(metricIds, weekIds);
    log.info("action=findByMetricIdsAndWeekIds.success count={}", values.size());
    return values;
  }

  @Override
  public Map<UUID, Map<UUID, MetricValue>> groupByMetricIdAndWeekId(List<MetricValue> values) {
    log.info("action=groupByMetricIdAndWeekId.start count={}", values == null ? 0 : values.size());
    if (CollectionUtils.isEmpty(values)) {
      log.info("action=groupByMetricIdAndWeekId.success count=0");
      return Collections.emptyMap();
    }

    log.debug("action=groupByMetricIdAndWeekId.branch.grouping count={}", values.size());
    Map<UUID, Map<UUID, MetricValue>> grouped = new HashMap<>();

    for (MetricValue value : values) {
      if (value == null
          || value.getMetric() == null
          || value.getMetric().getId() == null
          || value.getWeek() == null
          || value.getWeek().getId() == null) {

        log.warn("action=groupByMetricIdAndWeekId.validationFailed reason=missingMetricOrWeekData");
        continue;
      }

      UUID metricId = value.getMetric().getId();
      UUID weekId = value.getWeek().getId();

      grouped.computeIfAbsent(metricId, ignored -> new HashMap<>()).put(weekId, value);
    }

    log.info("action=groupByMetricIdAndWeekId.success count={}", grouped.size());
    return grouped;
  }
}
