package com.ces.eos.service;

import com.ces.eos.dto.response.WeekResponse;
import com.ces.eos.entity.Week;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WeekService {
  List<WeekResponse> getLast13Weeks();

  Week getOrCreateCurrentWeek();

  Optional<Week> getPreviousWeek(LocalDate startDate);

  Week getWeekById(UUID weekId);
}
