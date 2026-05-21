package com.ces.eos.service.impl;

import com.ces.eos.dto.response.WeekResponse;
import com.ces.eos.entity.Week;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.WeekMapper;
import com.ces.eos.repository.WeekRepository;
import com.ces.eos.service.WeekService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeekServiceImpl implements WeekService {

  private static final int WEEKS_IN_QUARTER = 13;
  private final WeekRepository weekRepository;
  private final WeekMapper weekMapper;

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public List<WeekResponse> getLast13Weeks() {
    log.info("action=getLast13Weeks.start");
    getOrCreateCurrentWeek();
    LocalDate today = LocalDate.now();

    Pageable pageable = PageRequest.of(0, WEEKS_IN_QUARTER, Sort.by("startDate").descending());
    log.debug("action=getLast13Weeks.repo.findByStartDateLessThanEqual date={}", today);
    List<Week> weeks = weekRepository.findByStartDateLessThanEqual(today, pageable);

    if (weeks.isEmpty()) {
      log.warn("action=getLast13Weeks.validationFailed reason=noWeeksFound");
    }

    List<WeekResponse> responses = weeks.stream().map(weekMapper::toWeekResponse).toList();
    log.info("action=getLast13Weeks.success count={}", responses.size());
    return responses;
  }

  @Override
  @Transactional
  public Week getOrCreateCurrentWeek() {
    log.debug("action=getOrCreateCurrentWeek.start");
    log.debug("action=getOrCreateCurrentWeek.repo.findCurrentWeek");
    Week week = weekRepository.findCurrentWeek().orElseGet(this::createCurrentWeek);
    log.debug("action=getOrCreateCurrentWeek.success weekId={}", week.getId());
    return week;
  }

  public Week createCurrentWeek() {
    log.debug("action=createCurrentWeek.start");
    LocalDate today = LocalDate.now();
    LocalDate startDate = today.with(DayOfWeek.MONDAY);
    LocalDate endDate = startDate.plusDays(6);
    log.debug(
        "action=createCurrentWeek.repo.insertWeekIfNotExists startDate={} endDate={}",
        startDate,
        endDate);
    weekRepository.insertWeekIfNotExists(startDate, endDate);
    log.debug("action=createCurrentWeek.repo.findCurrentWeek");
    return weekRepository
        .findCurrentWeek()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Failed to create or retrieve current week for period %s to %s"
                        .formatted(startDate, endDate)));
  }

  @Override
  public Optional<Week> getPreviousWeek(LocalDate startDate) {
    log.debug("action=getPreviousWeek.start startDate={}", startDate);
    log.debug(
        "action=getPreviousWeek.repo.findTopByStartDateLessThanOrderByStartDateDesc startDate={}",
        startDate);
    Optional<Week> week = weekRepository.findTopByStartDateLessThanOrderByStartDateDesc(startDate);
    log.debug("action=getPreviousWeek.success found={}", week.isPresent());
    return week;
  }

  @Override
  public Week getWeekById(UUID weekId) {
    log.debug("action=getWeekById.start weekId={}", weekId);
    log.debug("action=getWeekById.repo.findById weekId={}", weekId);
    Week week =
        weekRepository
            .findById(weekId)
            .orElseThrow(
                () -> {
                  log.warn("action=getWeekById.validationFailed weekId={}", weekId);
                  return new ResourceNotFoundException("Week not found with id: " + weekId);
                });
    log.debug("action=getWeekById.success weekId={}", week.getId());
    return week;
  }
}
