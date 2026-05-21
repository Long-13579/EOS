package com.ces.eos.scheduler;

import com.ces.eos.repository.WeekRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeekCreationScheduler {

  private final WeekRepository weekRepository;

  /**
   * Runs every Monday at 00:01 AM to create the current week Cron: second minute hour day month
   * day-of-week 0 1 0 * * MON = Every Monday at 00:01:00
   */
  @Scheduled(cron = "0 1 0 * * MON")
  @Transactional
  public void createCurrentWeek() {
    LocalDate today = LocalDate.now();

    // Ensure today is Monday
    if (today.getDayOfWeek() != DayOfWeek.MONDAY) {
      log.error("Scheduler ran on non-Monday: {}. This indicates a cron misconfiguration!", today);
      return;
    }

    LocalDate startDate = today;
    LocalDate endDate = today.plusDays(6); // Sunday

    // Create new week with conflict safety
    weekRepository.insertWeekIfNotExists(startDate, endDate);

    log.info("Attempted to create week: {} to {} (skipped if already exists)", startDate, endDate);
  }
}
