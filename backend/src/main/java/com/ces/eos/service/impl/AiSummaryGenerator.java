package com.ces.eos.service.impl;

import com.ces.eos.dto.response.L10MeetingChangeLogResponse;
import com.ces.eos.entity.L10Meeting;
import com.ces.eos.enums.AiSummaryStatus;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.repository.L10MeetingRepository;
import com.ces.eos.service.AiSummaryService;
import com.ces.eos.service.L10MeetingChangeLogService;
import com.ces.eos.util.ChangeLogDiffExtractor;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiSummaryGenerator {

  private final L10MeetingRepository l10MeetingRepository;
  private final L10MeetingChangeLogService l10MeetingChangeLogService;
  private final AiSummaryService aiSummaryService;
  private final ChangeLogDiffExtractor changeLogDiffExtractor;

  @Async("aiSummaryExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void generate(UUID meetingId) {
    log.info("Starting async AI summary generation for meetingId={}", meetingId);

    try {
      L10Meeting meeting = l10MeetingRepository.findById(meetingId)
          .orElseThrow(() -> {
            log.warn("Meeting not found for async summary, meetingId={}", meetingId);
            return new ResourceNotFoundException(
                Map.of("meetingId", List.of("L10 meeting not found with id: " + meetingId)));
          });

      if (meeting.getAiSummaryStatus() != AiSummaryStatus.PENDING) {
        log.info("Skipping summary generation, meetingId={} currentStatus={}",
            meetingId, meeting.getAiSummaryStatus());
        return;
      }

      String summary = aiSummaryService.generateSummary(meetingId);

      meeting.setAiSummary(summary);
      meeting.setAiSummaryStatus(AiSummaryStatus.COMPLETED);
      l10MeetingRepository.save(meeting);

      log.info("Async AI summary completed successfully for meetingId={}", meetingId);
    } catch (Exception e) {
      log.error("Async AI summary generation failed for meetingId={}", meetingId, e);
      setFailedWithFallback(meetingId);
    }
  }

  private void setFailedWithFallback(UUID meetingId) {
    try {
      L10Meeting meeting = l10MeetingRepository.findById(meetingId).orElse(null);
      if (meeting == null) {
        log.warn("Meeting not found when setting fallback summary, meetingId={}", meetingId);
        return;
      }
      if (meeting.getAiSummaryStatus() != AiSummaryStatus.PENDING) {
        return;
      }

      String fallbackSummary = generateFallbackSummary(meetingId);
      meeting.setAiSummary(fallbackSummary);
      meeting.setAiSummaryStatus(AiSummaryStatus.FAILED);
      l10MeetingRepository.save(meeting);

      log.info("Fallback summary set for meetingId={}", meetingId);
    } catch (Exception ex) {
      log.error("Failed to set fallback summary for meetingId={}", meetingId, ex);
    }
  }

  private String generateFallbackSummary(UUID meetingId) {
    try {
      List<L10MeetingChangeLogResponse> logs =
          l10MeetingChangeLogService.getChangeLogsByMeetingId(meetingId);
      var groups = changeLogDiffExtractor.extractChanges(logs);
      return changeLogDiffExtractor.formatSummary(groups);
    } catch (Exception e) {
      log.warn("Fallback summary generation also failed for meetingId={}", meetingId, e);
      return "Summary generation failed. No summary available.";
    }
  }
}
