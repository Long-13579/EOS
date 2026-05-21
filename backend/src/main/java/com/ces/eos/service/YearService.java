package com.ces.eos.service;

import com.ces.eos.dto.response.YearResponse;
import com.ces.eos.entity.CustomYear;
import java.util.List;
import java.util.UUID;

public interface YearService {
  List<YearResponse> getYears();

  void validateYearExists(UUID yearId);

  CustomYear getOrCreateYear(Integer year);
}
