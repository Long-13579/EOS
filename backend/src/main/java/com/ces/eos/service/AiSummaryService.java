package com.ces.eos.service;

import java.util.UUID;

public interface AiSummaryService {
  String generateSummary(UUID meetingId);
}
