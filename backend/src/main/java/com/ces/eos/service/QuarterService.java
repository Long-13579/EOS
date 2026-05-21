package com.ces.eos.service;

import com.ces.eos.dto.response.QuarterResponse;
import com.ces.eos.entity.Quarter;
import java.util.List;
import java.util.UUID;

public interface QuarterService {
  List<QuarterResponse> getQuarters();

  void validateQuarterExists(UUID quarterId);

  Quarter getQuarterById(UUID quarterId);
}
