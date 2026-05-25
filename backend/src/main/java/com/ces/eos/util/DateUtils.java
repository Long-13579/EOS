package com.ces.eos.util;

import com.ces.eos.entity.CustomYear;
import com.ces.eos.entity.Quarter;
import com.ces.eos.exception.ServerInternalException;
import java.time.Instant;
import java.time.MonthDay;
import java.time.Year;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateUtils {

  private static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
  private static final ZoneOffset DEFAULT_ZONE_OFFSET = ZoneOffset.UTC;
  private static final ZoneId DEFAULT_ZONE_ID = ZoneOffset.UTC;

  public static boolean isCurrentYear(CustomYear year) {
    Objects.requireNonNull(year, "Year must not be null");
    return year.getYear() == Year.now(DEFAULT_ZONE_OFFSET).getValue();
  }

  public static boolean isCurrentQuarter(Quarter quarter) {
    Objects.requireNonNull(quarter, "Quarter must not be null");
    return isMonthDayInQuarterRange(MonthDay.now(DEFAULT_ZONE_OFFSET), quarter);
  }

  public static boolean isDateWithinQuarterAndYear(Instant date, Quarter quarter, Integer year) {
    Objects.requireNonNull(date, "Date must not be null");
    Objects.requireNonNull(quarter, "Quarter must not be null");
    Objects.requireNonNull(year, "Year must not be null");

    int dateYear = date.atZone(DEFAULT_ZONE_OFFSET).getYear();
    MonthDay monthDay = MonthDay.from(date.atOffset(DEFAULT_ZONE_OFFSET));

    return isMonthDayInQuarterForYear(monthDay, dateYear, quarter, year);
  }

  public static MonthDay fromStringToMonthDay(String str) {
    return str == null ? null : MonthDay.parse(str, MONTH_DAY_FORMATTER);
  }

  public static String fromMonthDayToString(MonthDay monthDay) {
    return monthDay == null ? null : monthDay.format(MONTH_DAY_FORMATTER);
  }

  public static ZoneId resolveZoneId(String timezone) {
    if (timezone == null || timezone.isBlank()) {
      return DEFAULT_ZONE_ID;
    }
    return ZoneId.of(timezone);
  }

  public static LocalDate getTodayForTimezone(String timezone) {
    return LocalDate.now(resolveZoneId(timezone));
  }

  public static LocalDate getWeekStartDate(
      LocalDate meetingDate, LocalTime meetingTime, String timezone) {
    Objects.requireNonNull(meetingDate, "Meeting date must not be null");
    Objects.requireNonNull(meetingTime, "Meeting time must not be null");
    ZoneId zoneId = resolveZoneId(timezone);
    LocalDate zonedDate = meetingDate.atTime(meetingTime).atZone(zoneId).toLocalDate();
    return zonedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
  }

  private static boolean isMonthDayInQuarterRange(MonthDay monthDay, Quarter quarter) {
    validateQuarterDates(quarter);
    MonthDay start = quarter.getStartDate();
    MonthDay end = quarter.getEndDate();

    if (start.isAfter(end)) {
      return !monthDay.isBefore(start) || !monthDay.isAfter(end);
    }
    return !monthDay.isBefore(start) && !monthDay.isAfter(end);
  }

  private static boolean isMonthDayInQuarterForYear(
      MonthDay monthDay, int dateYear, Quarter quarter, int anchorYear) {

    validateQuarterDates(quarter);
    MonthDay start = quarter.getStartDate();
    MonthDay end = quarter.getEndDate();

    boolean quarterWrapsAcrossYearBoundary = start.isAfter(end);

    if (!quarterWrapsAcrossYearBoundary) {
      // Quarter is within a single year: both dateYear and monthDay must fall inside [start, end]
      return dateYear == anchorYear && !monthDay.isBefore(start) && !monthDay.isAfter(end);
    }

    boolean inStartHalf = dateYear == anchorYear && !monthDay.isBefore(start);
    boolean inEndHalf = dateYear == anchorYear + 1 && !monthDay.isAfter(end);
    return inStartHalf || inEndHalf;
  }

  private static void validateQuarterDates(Quarter quarter) {
    if (quarter.getStartDate() == null || quarter.getEndDate() == null) {
      throw new ServerInternalException(
          String.format("Quarter id=%s has null startDate or endDate", quarter.getId()));
    }
  }
}
