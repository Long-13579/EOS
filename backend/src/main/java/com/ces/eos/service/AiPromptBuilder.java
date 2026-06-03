package com.ces.eos.service;

import com.ces.eos.dto.response.L10MeetingChangeLogResponse;
import com.ces.eos.entity.L10Meeting;
import com.ces.eos.util.ChangeLogDiffExtractor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiPromptBuilder {

  private final ChangeLogDiffExtractor changeLogDiffExtractor;

  public String buildPrompt(L10Meeting meeting, List<L10MeetingChangeLogResponse> logs) {
    String changeLogSummary;
    try {
      var groups = changeLogDiffExtractor.extractChanges(logs);
      changeLogSummary = changeLogDiffExtractor.formatSummary(groups);
    } catch (Exception e) {
      changeLogSummary = buildFallbackChangeLog(logs);
    }

    return """
You are an AI assistant that generates professional L10 (Level 10) meeting summaries.

Below is the change log from the meeting, the key decisions made, and the cascading message.

Please synthesize these into a concise, professional meeting summary.

CHANGE LOG:
%s

KEY DECISIONS:
%s

CASCADING MESSAGE:
%s

Generate a well-structured meeting summary that captures the essence of what happened during this L10 meeting."""
        .formatted(changeLogSummary,
            meeting.getConcludeKeyDecisions() != null ? meeting.getConcludeKeyDecisions() : "None",
            meeting.getConcludeCascadingMessage() != null ? meeting.getConcludeCascadingMessage() : "None");
  }

  private String buildFallbackChangeLog(List<L10MeetingChangeLogResponse> logs) {
    StringBuilder sb = new StringBuilder();
    sb.append("During this meeting, the following changes were made:\n\n");
    if (logs.isEmpty()) {
      sb.append("No changes were made during this meeting.\n");
      return sb.toString();
    }
    for (int i = 0; i < logs.size(); i++) {
      L10MeetingChangeLogResponse log = logs.get(i);
      sb.append(i + 1).append(". [").append(log.entityType()).append("] ");
      if (log.beforeSnapshot() == null) {
        sb.append("Created\n   After: ").append(log.afterSnapshot()).append("\n");
      } else if (log.afterSnapshot() == null) {
        sb.append("Deleted\n   Before: ").append(log.beforeSnapshot()).append("\n");
      } else {
        sb.append("Updated\n   Before: ").append(log.beforeSnapshot())
            .append("\n   After:  ").append(log.afterSnapshot()).append("\n");
      }
      sb.append("\n");
    }
    return sb.toString();
  }
}
