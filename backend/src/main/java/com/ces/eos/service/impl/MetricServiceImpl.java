package com.ces.eos.service.impl;

import com.ces.eos.dto.request.CreateMetricRequest;
import com.ces.eos.dto.request.UpdateMetricRequest;
import com.ces.eos.dto.response.MetricResponse;
import com.ces.eos.dto.response.MetricValueBaseResponse;
import com.ces.eos.dto.response.TrendDataPointResponse;
import com.ces.eos.dto.response.TrendsTabMetricListResponse;
import com.ces.eos.dto.response.TrendsTabMetricResponse;
import com.ces.eos.dto.response.WeekResponse;
import com.ces.eos.entity.Metric;
import com.ces.eos.entity.MetricValue;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.User;
import com.ces.eos.entity.Week;
import com.ces.eos.enums.MetricOperator;
import com.ces.eos.enums.MetricUnit;
import com.ces.eos.exception.BadRequestException;
import com.ces.eos.exception.ConflictException;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.MetricMapper;
import com.ces.eos.mapper.MetricValueMapper;
import com.ces.eos.repository.MetricRepository;
import com.ces.eos.repository.MetricValueRepository;
import com.ces.eos.service.MetricService;
import com.ces.eos.service.MetricValueService;
import com.ces.eos.service.TeamService;
import com.ces.eos.service.UserService;
import com.ces.eos.service.WeekService;
import com.ces.eos.service.L10MeetingChangeLogService;
import com.ces.eos.util.EnumParserUtil;
import com.ces.eos.util.MetricUtil;
import tools.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MetricServiceImpl implements MetricService {
  private final MetricRepository metricRepository;
  private final MetricValueRepository metricValueRepository;
  private final MetricValueService metricValueService;
  private final MetricMapper metricMapper;
  private final MetricValueMapper metricValueMapper;
  private final WeekService weekService;
  private final UserService userService;
  private final TeamService teamService;
  private final L10MeetingChangeLogService l10MeetingChangeLogService;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public MetricResponse addMetric(CreateMetricRequest request, UUID creatorId) {
    log.info("action=addMetric.start creatorId={} teamId={}", creatorId, request.teamId());
    Metric metric = metricMapper.toEntity(request);
    validateMetric(metric.getUnit(), metric.getOperator(), metric.getGoal());

    log.debug(
        "action=addMetric.service.getUserByIdAndTeamId ownerId={} teamId={}",
        request.ownerId(),
        request.teamId());
    User owner = userService.getUserByIdAndTeamId(request.ownerId(), request.teamId());
    log.debug("action=addMetric.service.getUserById creatorId={}", creatorId);
    User creator = userService.getUserById(creatorId);
    log.debug("action=addMetric.service.getTeamById teamId={}", request.teamId());
    Team team = teamService.getTeamById(request.teamId());

    metric.setOwner(owner);
    metric.setCreatedBy(creator);
    metric.setUpdatedBy(creator);
    metric.setTeam(team);

    log.debug("action=addMetric.repo.save teamId={}", request.teamId());
    Metric savedMetric = metricRepository.save(metric);
    MetricValue defaultMetricValue = addDefaultMetricValueForCurrentWeek(savedMetric);

    // Log the new metric creation
    log.debug("action=addMetric.logChange metricId={}", savedMetric.getId());
    l10MeetingChangeLogService.logChange(
        request.teamId(),
        "METRIC",
        savedMetric.getId(),
        null,
        objectMapper.valueToTree(metricMapper.toMetricResponse(savedMetric, defaultMetricValue, null)).toString());

    log.info("action=addMetric.success metricId={}", savedMetric.getId());
    return metricMapper.toMetricResponse(savedMetric, defaultMetricValue, null);
  }

  @Override
  public List<MetricResponse> listMetricsByTeamAndWeek(UUID teamId, UUID weekId, Boolean showArchived) {
    log.info("action=listMetricsByTeamAndWeek.start teamId={} weekId={} showArchived={}", teamId, weekId, showArchived);
    log.debug("action=listMetricsByTeamAndWeek.repo.findByTeamIdAndIsArchived teamId={} showArchived={}", teamId, showArchived);
    List<Metric> metrics = metricRepository.findByTeamIdAndIsArchived(teamId, Boolean.TRUE.equals(showArchived));
    List<MetricResponse> responses = processMetricsForWeek(metrics, weekId, "teamId=" + teamId);
    log.info(
        "action=listMetricsByTeamAndWeek.success teamId={} count={}", teamId, responses.size());
    return responses;
  }

  @Override
  public List<MetricResponse> listMyAssignedMetrics(UUID userId, UUID weekId) {
    log.info("action=listMyAssignedMetrics.start userId={} weekId={}", userId, weekId);
    log.debug("action=listMyAssignedMetrics.repo.findByOwnerId userId={}", userId);
    List<Metric> metrics = metricRepository.findByOwnerId(userId);
    List<MetricResponse> responses = processMetricsForWeek(metrics, weekId, "userId=" + userId);
    log.info("action=listMyAssignedMetrics.success userId={} count={}", userId, responses.size());
    return responses;
  }

  private List<MetricResponse> processMetricsForWeek(
      List<Metric> metrics, UUID weekId, String contextLog) {
    if (metrics.isEmpty()) {
      log.debug("action=processMetricsForWeek.branch.noMetrics context={}", contextLog);
      return Collections.emptyList();
    }
    log.debug(
        "action=processMetricsForWeek.branch.metricsFound count={} context={}",
        metrics.size(),
        contextLog);

    log.debug("action=processMetricsForWeek.service.getWeekById weekId={}", weekId);
    Week currentWeek = weekService.getWeekById(weekId);

    log.debug(
        "action=processMetricsForWeek.service.getPreviousWeek startDate={}",
        currentWeek.getStartDate());
    UUID previousWeekId =
        weekService.getPreviousWeek(currentWeek.getStartDate()).map(Week::getId).orElse(null);
    log.debug(
        "action=processMetricsForWeek.branch.previousWeekResolved previousWeekId={}",
        previousWeekId);

    List<UUID> targetWeekIds = Stream.of(weekId, previousWeekId).filter(Objects::nonNull).toList();
    List<UUID> metricIds = metrics.stream().map(Metric::getId).toList();

    log.debug(
        "action=processMetricsForWeek.service.findByMetricIdsAndWeekIds metricCount={} weekCount={}",
        metricIds.size(),
        targetWeekIds.size());
    List<MetricValue> allValues =
        metricValueService.findByMetricIdsAndWeekIds(metricIds, targetWeekIds);
    log.debug(
        "action=processMetricsForWeek.service.groupByMetricIdAndWeekId valueCount={}",
        allValues.size());
    Map<UUID, Map<UUID, MetricValue>> valuesByMetricAndWeek =
        metricValueService.groupByMetricIdAndWeekId(allValues);

    List<MetricResponse> responses =
        metrics.stream()
            .map(
                metric -> {
                  Map<UUID, MetricValue> weekValues =
                      valuesByMetricAndWeek.getOrDefault(metric.getId(), Collections.emptyMap());
                  return metricMapper.toMetricResponse(
                      metric, weekValues.get(weekId), weekValues.get(previousWeekId));
                })
            .toList();

    log.debug(
        "action=processMetricsForWeek.success count={} context={}", responses.size(), contextLog);
    return responses;
  }

  @Override
  public TrendsTabMetricListResponse listTrendsTabMetricsByTeam(UUID teamId) {
    log.info("action=listTrendsTabMetricsByTeam.start teamId={}", teamId);

    log.debug("action=listTrendsTabMetricsByTeam.repo.findByTeamIdAndIsArchivedFalse teamId={}", teamId);
    List<Metric> metrics = metricRepository.findByTeamIdAndIsArchivedFalse(teamId);
    if (metrics.isEmpty()) {
      log.debug("action=listTrendsTabMetricsByTeam.branch.noMetrics teamId={}", teamId);
      log.info("action=listTrendsTabMetricsByTeam.success teamId={} count=0", teamId);
      return new TrendsTabMetricListResponse(Collections.emptyList());
    }
    log.debug(
        "action=listTrendsTabMetricsByTeam.branch.metricsFound teamId={} count={}",
        teamId,
        metrics.size());

    log.debug("action=listTrendsTabMetricsByTeam.service.getLast13Weeks");
    List<WeekResponse> weeks =
        weekService.getLast13Weeks().stream()
            .sorted(Comparator.comparing(WeekResponse::startDate))
            .toList();

    if (weeks.isEmpty()) {
      log.warn(
          "action=listTrendsTabMetricsByTeam.validationFailed reason=noWeeks teamId={}", teamId);
      return new TrendsTabMetricListResponse(Collections.emptyList());
    }

    List<UUID> weekIds = weeks.stream().map(WeekResponse::id).toList();
    List<UUID> metricIds = metrics.stream().map(Metric::getId).toList();

    log.debug(
        "action=listTrendsTabMetricsByTeam.service.findByMetricIdsAndWeekIds metricCount={} weekCount={}",
        metricIds.size(),
        weekIds.size());
    List<MetricValue> allValues = metricValueService.findByMetricIdsAndWeekIds(metricIds, weekIds);
    log.debug(
        "action=listTrendsTabMetricsByTeam.service.groupByMetricIdAndWeekId valueCount={}",
        allValues.size());
    Map<UUID, Map<UUID, MetricValue>> valuesByMetricAndWeek =
        metricValueService.groupByMetricIdAndWeekId(allValues);

    List<TrendsTabMetricResponse> items =
        metrics.stream()
            .map(
                metric -> {
                  Map<UUID, MetricValue> weekValues =
                      valuesByMetricAndWeek.getOrDefault(metric.getId(), Collections.emptyMap());

                  List<TrendDataPointResponse> dataPoints =
                      weeks.stream()
                          .map(
                              week -> {
                                MetricValue metricValue = weekValues.get(week.id());
                                MetricValueBaseResponse metricValueResponse =
                                    metricValue == null
                                        ? null
                                        : metricValueMapper.toMetricValueBaseResponse(metricValue);
                                return new TrendDataPointResponse(week, metricValueResponse);
                              })
                          .toList();

                  return metricMapper.toTrendsTabMetricResponse(metric, dataPoints);
                })
            .toList();

    log.info("action=listTrendsTabMetricsByTeam.success teamId={} count={}", teamId, items.size());
    return new TrendsTabMetricListResponse(items);
  }

  private MetricValue addDefaultMetricValueForCurrentWeek(Metric metric) {
    log.debug("action=addDefaultMetricValueForCurrentWeek.service.getOrCreateCurrentWeek");
    Week currentWeek = weekService.getOrCreateCurrentWeek();
    log.debug("action=addDefaultMetricValueForCurrentWeek.service.addDefaultMetricValueForWeek");
    return metricValueService.addDefaultMetricValueForWeek(metric, currentWeek);
  }

  private void validateMetric(MetricUnit unit, MetricOperator operator, String goal) {
    try {
      MetricUtil.validateMetricCreation(unit, operator, goal);
    } catch (IllegalArgumentException e) {
      log.warn("action=validateMetric.validationFailed unit={} operator={}", unit, operator);
      throw new BadRequestException(e.getMessage());
    }
  }

  @Override
  @Transactional
  public MetricResponse updateMetric(UUID metricId, UpdateMetricRequest request, UUID updaterId) {
    log.info("action=updateMetric.start metricId={} updaterId={}", metricId, updaterId);
    Metric metric = getMetricById(metricId);

    if (Boolean.TRUE.equals(metric.getIsArchived())) {
      log.warn("action=updateMetric.validationFailed reason=archived metricId={}", metricId);
      throw new ConflictException(
          Map.of(
              "metricId", List.of("Cannot update an archived metric. Please unarchive it first.")));
    }

    MetricOperator newOperator =
        request.operator() == null
            ? null
            : EnumParserUtil.parseEnum(MetricOperator.class, request.operator(), "operator");

    validateMetric(metric.getUnit(), newOperator, request.goal());

    UUID teamId = metric.getTeam().getId();
    log.debug(
        "action=updateMetric.service.getUserByIdAndTeamId ownerId={} teamId={}",
        request.ownerId(),
        teamId);
    User newOwner = userService.getUserByIdAndTeamId(request.ownerId(), teamId);
    log.debug("action=updateMetric.service.getUserById updaterId={}", updaterId);
    User updater = userService.getUserById(updaterId);

    // Capture before snapshot
    var beforeSnapshot = objectMapper.valueToTree(metricMapper.toMetricResponse(metric, null, null)).toString();

    metric.setName(request.name());
    metric.setGoal(request.goal());
    metric.setOperator(newOperator);
    metric.setOwner(newOwner);
    metric.setUpdatedBy(updater);

    log.debug("action=updateMetric.repo.save metricId={}", metricId);
    Metric updatedMetric = metricRepository.save(metric);

    // Log the metric update
    log.debug("action=updateMetric.logChange metricId={}", updatedMetric.getId());
    l10MeetingChangeLogService.logChange(
        teamId,
        "METRIC",
        updatedMetric.getId(),
        beforeSnapshot,
        objectMapper.valueToTree(metricMapper.toMetricResponse(updatedMetric, null, null)).toString());

    log.info("action=updateMetric.success metricId={}", updatedMetric.getId());

    return metricMapper.toMetricResponse(updatedMetric, null, null);
  }

  @Override
  @Transactional
  public MetricResponse updateMetricArchiveStatus(UUID metricId, Boolean isArchived) {
    log.info("action=updateMetricArchiveStatus.start metricId={} isArchived={}", metricId, isArchived);
    Metric metric = getMetricById(metricId);
    if (metric.getIsArchived() == isArchived) {
      log.debug("action=updateMetricArchiveStatus.branch.noChange metricId={}", metricId);
      log.info("action=updateMetricArchiveStatus.success metricId={}", metricId);
      return metricMapper.toMetricResponse(metric, null, null);
    }

    var beforeSnapshot = objectMapper.valueToTree(metricMapper.toMetricResponse(metric, null, null)).toString();

    metric.setIsArchived(isArchived);
    log.debug("action=updateMetricArchiveStatus.repo.save metricId={}", metricId);
    Metric updatedMetric = metricRepository.save(metric);

    log.debug("action=updateMetricArchiveStatus.logChange metricId={}", updatedMetric.getId());
    l10MeetingChangeLogService.logChange(
        metric.getTeam().getId(),
        "METRIC",
        updatedMetric.getId(),
        beforeSnapshot,
        objectMapper.valueToTree(metricMapper.toMetricResponse(updatedMetric, null, null)).toString());

    log.info("action=updateMetricArchiveStatus.success metricId={}", updatedMetric.getId());
    return metricMapper.toMetricResponse(updatedMetric, null, null);
  }

  @Override
  @Transactional
  public void deleteMetricById(UUID metricId) {
    log.info("action=deleteMetricById.start metricId={}", metricId);
    Metric metric = getMetricById(metricId);

    log.debug("action=deleteMetricById.logChange metricId={}", metric.getId());
    l10MeetingChangeLogService.logChange(
        metric.getTeam().getId(),
        "METRIC",
        metric.getId(),
        objectMapper.valueToTree(metricMapper.toMetricResponse(metric, null, null)).toString(),
        null);

    log.debug("action=deleteMetricById.repo.deleteValues metricId={}", metricId);
    metricValueRepository.deleteByMetricId(metricId);

    log.debug("action=deleteMetricById.repo.delete metricId={}", metricId);
    metricRepository.delete(metric);
    log.info("action=deleteMetricById.success metricId={}", metricId);
  }

  @Override
  public Metric getMetricById(UUID metricId) {
    log.debug("action=getMetricById.start metricId={}", metricId);
    log.debug("action=getMetricById.repo.findByIdWithTeam metricId={}", metricId);
    Metric metric =
        metricRepository
            .findByIdWithTeam(metricId)
            .orElseThrow(
                () -> {
                  log.warn("action=getMetricById.validationFailed metricId={}", metricId);
                  return new ResourceNotFoundException(
                      Map.of(
                          "metricId",
                          List.of(String.format("Metric not found with id: %s", metricId))));
                });
    log.debug("action=getMetricById.success metricId={}", metric.getId());
    return metric;
  }
}
