package com.ces.eos.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.ces.eos.dto.response.L10MeetingChangeLogResponse;
import com.ces.eos.util.ChangeLogDiffExtractor.ChangeType;
import com.ces.eos.util.ChangeLogDiffExtractor.EntityChangeGroup;
import com.ces.eos.util.ChangeLogDiffExtractor.ImpactLevel;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ChangeLogDiffExtractorTest {

  private ChangeLogDiffExtractor extractor;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    extractor = new ChangeLogDiffExtractor(objectMapper);
  }

  @Test
  void extractChanges_emptyLogs_returnsEmptyGroups() {
    var result = extractor.extractChanges(List.of());
    assertThat(result).isEmpty();
  }

  @Test
  void extractChanges_creationLog_extractsCreationChanges() {
    UUID entityId = UUID.randomUUID();
    var log = new L10MeetingChangeLogResponse(
        UUID.randomUUID(), UUID.randomUUID(), "ISSUE", entityId,
        null,
        "{\"title\":\"Fix login bug\",\"description\":\"User cannot log in\",\"id\":\""
            + entityId + "\",\"isArchived\":false}",
        null, null);

    var groups = extractor.extractChanges(List.of(log));

    assertThat(groups).hasSize(1);
    EntityChangeGroup group = groups.getFirst();
    assertThat(group.entityType()).isEqualTo("ISSUE");
    assertThat(group.changes()).hasSize(1);
    assertThat(group.changes().getFirst().changeType()).isEqualTo(ChangeType.CREATED);
    assertThat(group.changes().getFirst().impact()).isEqualTo(ImpactLevel.HIGH);
    var details = group.changes().getFirst().details();
    assertThat(details).anyMatch(d -> "title".equals(d.fieldName()) && "Fix login bug".equals(d.newValue()));
    assertThat(details).anyMatch(d -> "description".equals(d.fieldName()) && "User cannot log in".equals(d.newValue()));
    assertThat(details).anyMatch(d -> "isArchived".equals(d.fieldName()) && "false".equals(d.newValue()));
  }

  @Test
  void extractChanges_deletionLog_extractsDeletionChanges() {
    UUID entityId = UUID.randomUUID();
    var log = new L10MeetingChangeLogResponse(
        UUID.randomUUID(), UUID.randomUUID(), "ROCK", entityId,
        "{\"title\":\"Q1 Revenue\",\"status\":\"ON_TRACK\",\"id\":\"" + entityId + "\"}",
        null,
        null, null);

    var groups = extractor.extractChanges(List.of(log));

    assertThat(groups).hasSize(1);
    assertThat(groups.getFirst().changes().getFirst().changeType()).isEqualTo(ChangeType.DELETED);
    assertThat(groups.getFirst().changes().getFirst().details())
        .anyMatch(d -> "title".equals(d.fieldName()) && "Q1 Revenue".equals(d.oldValue()));
  }

  @Test
  void extractChanges_updateLog_computesFieldDiffForRock() {
    UUID entityId = UUID.randomUUID();
    var log = new L10MeetingChangeLogResponse(
        UUID.randomUUID(), UUID.randomUUID(), "ROCK", entityId,
        "{\"title\":\"Q1 Revenue\",\"status\":\"ON_TRACK\",\"id\":\"" + entityId + "\"}",
        "{\"title\":\"Q1 Revenue\",\"status\":\"AT_RISK\",\"id\":\"" + entityId + "\"}",
        null, null);

    var groups = extractor.extractChanges(List.of(log));

    assertThat(groups).hasSize(1);
    var change = groups.getFirst().changes().getFirst();
    assertThat(change.changeType()).isEqualTo(ChangeType.UPDATED);
    assertThat(change.impact()).isEqualTo(ImpactLevel.HIGH);
    var details = change.details();
    assertThat(details).anyMatch(d ->
        "status".equals(d.fieldName()) && "ON_TRACK".equals(d.oldValue()) && "AT_RISK".equals(d.newValue()));
  }

  @Test
  void extractChanges_updateLog_unchangedFieldsAreExcluded() {
    UUID entityId = UUID.randomUUID();
    var log = new L10MeetingChangeLogResponse(
        UUID.randomUUID(), UUID.randomUUID(), "ISSUE", entityId,
        "{\"title\":\"Same title\",\"id\":\"" + entityId + "\"}",
        "{\"title\":\"Same title\",\"id\":\"" + entityId + "\"}",
        null, null);

    var groups = extractor.extractChanges(List.of(log));

    assertThat(groups).isEmpty();
  }

  @Test
  void extractChanges_technicalFieldsAreFilteredOut() {
    UUID entityId = UUID.randomUUID();
    var log = new L10MeetingChangeLogResponse(
        UUID.randomUUID(), UUID.randomUUID(), "ISSUE", entityId,
        "{\"title\":\"Title\",\"createdAt\":\"2026-01-01T00:00:00Z\",\"updatedAt\":\"2026-01-01T00:00:00Z\",\"id\":\""
            + entityId + "\"}",
        "{\"title\":\"Title\",\"createdAt\":\"2026-01-02T00:00:00Z\",\"updatedAt\":\"2026-01-02T00:00:00Z\",\"id\":\""
            + entityId + "\"}",
        null, null);

    var groups = extractor.extractChanges(List.of(log));

    assertThat(groups).isEmpty();
  }

  @Test
  void extractChanges_multipleLogs_groupsByEntityType() {
    UUID issueId = UUID.randomUUID();
    UUID rockId = UUID.randomUUID();
    UUID meetingId = UUID.randomUUID();

    var logs = List.of(
        new L10MeetingChangeLogResponse(
            UUID.randomUUID(), meetingId, "ISSUE", issueId,
            null,
            "{\"title\":\"New issue\",\"id\":\"" + issueId + "\"}",
            null, null),
        new L10MeetingChangeLogResponse(
            UUID.randomUUID(), meetingId, "ROCK", rockId,
            "{\"title\":\"Old rock\",\"status\":\"ON_TRACK\",\"id\":\"" + rockId + "\"}",
            "{\"title\":\"Old rock\",\"status\":\"AT_RISK\",\"id\":\"" + rockId + "\"}",
            null, null)
    );

    var groups = extractor.extractChanges(logs);

    assertThat(groups).hasSize(2);
    assertThat(groups.get(0).entityType()).isEqualTo("ISSUE");
    assertThat(groups.get(1).entityType()).isEqualTo("ROCK");
  }

  @Test
  void extractChanges_nestedObjectChanges_detectsOwnerChange() {
    UUID entityId = UUID.randomUUID();
    var log = new L10MeetingChangeLogResponse(
        UUID.randomUUID(), UUID.randomUUID(), "ROCK", entityId,
        "{\"title\":\"Rock\",\"owner\":{\"id\":\"1111\",\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"email\":\"alice@test.com\"},\"id\":\""
            + entityId + "\"}",
        "{\"title\":\"Rock\",\"owner\":{\"id\":\"2222\",\"firstName\":\"Bob\",\"lastName\":\"Jones\",\"email\":\"bob@test.com\"},\"id\":\""
            + entityId + "\"}",
        null, null);

    var groups = extractor.extractChanges(List.of(log));

    assertThat(groups).hasSize(1);
    var details = groups.getFirst().changes().getFirst().details();
    assertThat(details).anyMatch(d ->
        "owner".equals(d.fieldName())
            && "Alice Smith".equals(d.oldValue())
            && "Bob Jones".equals(d.newValue()));
    assertThat(groups.getFirst().changes().getFirst().impact()).isEqualTo(ImpactLevel.HIGH);
  }

  @Test
  void formatSummary_withChanges_returnsFormattedOutput() {
    UUID entityId = UUID.randomUUID();
    var log = new L10MeetingChangeLogResponse(
        UUID.randomUUID(), UUID.randomUUID(), "ROCK", entityId,
        "{\"title\":\"Bug fix\",\"status\":\"ON_TRACK\",\"id\":\"" + entityId + "\"}",
        "{\"title\":\"Bug fix\",\"status\":\"AT_RISK\",\"id\":\"" + entityId + "\"}",
        null, null);

    var groups = extractor.extractChanges(List.of(log));
    var summary = extractor.formatSummary(groups);

    assertThat(summary).contains("ROCK");
    assertThat(summary).contains("UPDATED");
    assertThat(summary).contains("\"Bug fix\"");
    assertThat(summary).contains("Status");
    assertThat(summary).contains("ON_TRACK");
    assertThat(summary).contains("AT_RISK");
  }

  @Test
  void formatSummary_updateOnlyNonTitleFields_showsIdentifierFromSnapshot() {
    UUID entityId = UUID.randomUUID();
    // Only status changes, title stays the same
    var log = new L10MeetingChangeLogResponse(
        UUID.randomUUID(), UUID.randomUUID(), "ROCK", entityId,
        "{\"title\":\"Q1 Revenue Target\",\"status\":\"ON_TRACK\",\"id\":\"" + entityId + "\"}",
        "{\"title\":\"Q1 Revenue Target\",\"status\":\"OFF_TRACK\",\"id\":\"" + entityId + "\"}",
        null, null);

    var groups = extractor.extractChanges(List.of(log));
    var summary = extractor.formatSummary(groups);

    assertThat(summary).contains("Q1 Revenue Target");
    assertThat(summary).contains("OFF_TRACK");
    assertThat(summary).contains("ON_TRACK");
  }

  @Test
  void formatSummary_emptyChanges_returnsNoChangesMessage() {
    var summary = extractor.formatSummary(List.of());
    assertThat(summary).contains("No changes were made");
  }

  @Test
  void extractChanges_malformedJson_returnsEmptyAndDoesNotThrow() {
    var log = new L10MeetingChangeLogResponse(
        UUID.randomUUID(), UUID.randomUUID(), "ISSUE", UUID.randomUUID(),
        "{\"title\":\"Before}",
        "{\"title\":\"After\"}",
        null, null);

    var groups = extractor.extractChanges(List.of(log));
    assertThat(groups).isEmpty();
  }
}
