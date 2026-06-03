package com.ces.eos.service.impl;

import com.ces.eos.dto.response.L10MeetingChangeLogResponse;
import com.ces.eos.entity.L10Meeting;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.repository.L10MeetingRepository;
import com.ces.eos.service.AiPromptBuilder;
import com.ces.eos.service.AiSummaryService;
import com.ces.eos.service.L10MeetingChangeLogService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSummaryServiceImpl implements AiSummaryService {

  private final L10MeetingRepository l10MeetingRepository;
  private final L10MeetingChangeLogService l10MeetingChangeLogService;
  private final AiPromptBuilder aiPromptBuilder;
  private final AzureOpenAiClient azureOpenAiClient;

  @Override
  public String generateSummary(UUID meetingId) {
    L10Meeting meeting = l10MeetingRepository.findByIdWithRelations(meetingId)
        .orElseThrow(() -> {
          log.warn("Meeting not found for summary generation, meetingId={}", meetingId);
          return new ResourceNotFoundException(
              Map.of("meetingId", List.of("L10 meeting not found with id: " + meetingId)));
        });

    List<L10MeetingChangeLogResponse> logs =
        l10MeetingChangeLogService.getChangeLogsByMeetingId(meetingId);

    String prompt = aiPromptBuilder.buildPrompt(meeting, logs);
    return azureOpenAiClient.generateSummary(prompt);
  }
}
