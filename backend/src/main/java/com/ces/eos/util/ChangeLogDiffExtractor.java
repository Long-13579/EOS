package com.ces.eos.util;

import com.ces.eos.dto.response.L10MeetingChangeLogResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangeLogDiffExtractor {

  private static final Map<String, Set<String>> ENTITY_SIGNIFICANT_FIELDS = new HashMap<>();

  private static final Map<String, String> FIELD_DISPLAY_NAMES = new LinkedHashMap<>();

  private static final Set<String> STATUS_LIKE_FIELDS = Set.of("status");
  private static final Set<String> ASSIGNMENT_LIKE_FIELDS = Set.of("owner", "assignees", "creator");

  static {
    ENTITY_SIGNIFICANT_FIELDS.put("ISSUE", Set.of(
        "title", "description", "isArchived", "issueType", "totalTodosCount"));
    ENTITY_SIGNIFICANT_FIELDS.put("ROCK", Set.of(
        "title", "description", "status", "category", "isArchived", "dueDate", "owner"));
    ENTITY_SIGNIFICANT_FIELDS.put("TODO", Set.of(
        "title", "description", "status", "isArchived", "dueDate", "assignees"));
    ENTITY_SIGNIFICANT_FIELDS.put("HEADLINE", Set.of("title", "isArchived"));
    ENTITY_SIGNIFICANT_FIELDS.put("METRIC", Set.of(
        "name", "goal", "unit", "operator", "lastValue", "owner"));
    ENTITY_SIGNIFICANT_FIELDS.put("METRIC_VALUE", Set.of("value", "isGoalMet"));

    FIELD_DISPLAY_NAMES.put("title", "Title");
    FIELD_DISPLAY_NAMES.put("description", "Description");
    FIELD_DISPLAY_NAMES.put("status", "Status");
    FIELD_DISPLAY_NAMES.put("isArchived", "Archived");
    FIELD_DISPLAY_NAMES.put("dueDate", "Due date");
    FIELD_DISPLAY_NAMES.put("category", "Category");
    FIELD_DISPLAY_NAMES.put("name", "Name");
    FIELD_DISPLAY_NAMES.put("goal", "Goal");
    FIELD_DISPLAY_NAMES.put("unit", "Unit");
    FIELD_DISPLAY_NAMES.put("operator", "Operator");
    FIELD_DISPLAY_NAMES.put("lastValue", "Last value");
    FIELD_DISPLAY_NAMES.put("value", "Value");
    FIELD_DISPLAY_NAMES.put("isGoalMet", "Goal met");
    FIELD_DISPLAY_NAMES.put("owner", "Owner");
    FIELD_DISPLAY_NAMES.put("creator", "Creator");
    FIELD_DISPLAY_NAMES.put("assignees", "Assignees");
    FIELD_DISPLAY_NAMES.put("issueType", "Issue type");
    FIELD_DISPLAY_NAMES.put("totalTodosCount", "Total todos");
  }

  public record ChangeDetail(
      String fieldName, String displayName, Object oldValue, Object newValue, ChangeType changeType) {}

  public record CategorizedChange(
      String entityType, UUID entityId, ChangeType changeType,
      List<ChangeDetail> details, ImpactLevel impact,
      String identifier) {}

  public record EntityChangeGroup(String entityType, List<CategorizedChange> changes) {}

  public enum ChangeType { CREATED, UPDATED, DELETED }
  public enum ImpactLevel { HIGH, MEDIUM, LOW }

  private final ObjectMapper objectMapper;

  public List<EntityChangeGroup> extractChanges(List<L10MeetingChangeLogResponse> logs) {
    Map<String, List<CategorizedChange>> grouped = new LinkedHashMap<>();
    for (L10MeetingChangeLogResponse entry : logs) {
      List<CategorizedChange> changes = processLog(entry);
      for (CategorizedChange change : changes) {
        grouped.computeIfAbsent(change.entityType(), k -> new ArrayList<>()).add(change);
      }
    }
    return grouped.entrySet().stream()
        .map(e -> new EntityChangeGroup(e.getKey(), e.getValue()))
        .toList();
  }

  private List<CategorizedChange> processLog(L10MeetingChangeLogResponse entry) {
    String beforeStr = entry.beforeSnapshot();
    String afterStr = entry.afterSnapshot();
    UUID entityId = entry.entityId();
    String entityType = entry.entityType();

    try {
      if (beforeStr == null && afterStr != null) {
        return handleCreate(afterStr, entityType, entityId);
      } else if (beforeStr != null && afterStr == null) {
        return handleDelete(beforeStr, entityType, entityId);
      } else if (beforeStr != null) {
        return handleUpdate(beforeStr, afterStr, entityType, entityId);
      }
    } catch (Exception e) {
      log.warn("Failed to process change log for entityType={} entityId={}",
          entityType, entityId, e);
    }
    return List.of();
  }

  private List<CategorizedChange> handleCreate(
      String afterStr, String entityType, UUID entityId) throws Exception {
    JsonNode after = objectMapper.readTree(afterStr);
    String identifier = extractIdentifierFromNode(after);
    List<ChangeDetail> details = new ArrayList<>();
    Set<String> significant = getSignificantFields(entityType);
    for (String field : significant) {
      JsonNode value = after.get(field);
      if (value != null && !value.isNull()) {
        details.add(new ChangeDetail(
            field, getDisplayName(field), null, formatNode(value), ChangeType.CREATED));
      }
    }
    if (!details.isEmpty()) {
      return List.of(new CategorizedChange(
          entityType, entityId, ChangeType.CREATED, details, ImpactLevel.HIGH, identifier));
    }
    return List.of();
  }

  private List<CategorizedChange> handleDelete(
      String beforeStr, String entityType, UUID entityId) throws Exception {
    JsonNode before = objectMapper.readTree(beforeStr);
    String identifier = extractIdentifierFromNode(before);
    List<ChangeDetail> details = new ArrayList<>();
    Set<String> significant = getSignificantFields(entityType);
    for (String field : significant) {
      JsonNode value = before.get(field);
      if (value != null && !value.isNull()) {
        details.add(new ChangeDetail(
            field, getDisplayName(field), formatNode(value), null, ChangeType.DELETED));
      }
    }
    if (!details.isEmpty()) {
      return List.of(new CategorizedChange(
          entityType, entityId, ChangeType.DELETED, details, ImpactLevel.HIGH, identifier));
    }
    return List.of();
  }

  private List<CategorizedChange> handleUpdate(
      String beforeStr, String afterStr, String entityType, UUID entityId) throws Exception {
    JsonNode before = objectMapper.readTree(beforeStr);
    JsonNode after = objectMapper.readTree(afterStr);
    String identifier = extractIdentifierFromNode(after);
    List<ChangeDetail> details = new ArrayList<>();
    Set<String> significant = getSignificantFields(entityType);

    Collection<String> allFields = new ArrayList<>(significant);
    for (String field : allFields) {
      JsonNode beforeVal = before.get(field);
      JsonNode afterVal = after.get(field);
      if (areEqual(beforeVal, afterVal)) continue;
      String oldVal = beforeVal != null && !beforeVal.isNull() ? formatNode(beforeVal) : null;
      String newVal = afterVal != null && !afterVal.isNull() ? formatNode(afterVal) : null;
      details.add(new ChangeDetail(
          field, getDisplayName(field), oldVal, newVal, ChangeType.UPDATED));
    }

    if (!details.isEmpty()) {
      ImpactLevel impact = computeImpact(details);
      return List.of(new CategorizedChange(
          entityType, entityId, ChangeType.UPDATED, details, impact, identifier));
    }
    return List.of();
  }

  private static String extractIdentifierFromNode(JsonNode node) {
    if (node == null) return null;
    if (node.has("title") && !node.get("title").isNull()) {
      return node.get("title").asText();
    }
    if (node.has("name") && !node.get("name").isNull()) {
      return node.get("name").asText();
    }
    return null;
  }

  private static boolean areEqual(JsonNode a, JsonNode b) {
    if (a == null && b == null) return true;
    if (a == null || b == null) return false;
    return a.equals(b);
  }

  private String formatNode(JsonNode node) {
    if (node == null || node.isNull()) return "none";
    if (node.isTextual()) return node.asText();
    if (node.isNumber() || node.isBoolean()) return node.asText();
    if (node.isObject()) {
      if (node.has("name")) return node.get("name").asText();
      if (node.has("firstName") && node.has("lastName")) {
        return node.get("firstName").asText() + " " + node.get("lastName").asText();
      }
      if (node.has("title")) return node.get("title").asText();
      return node.toString();
    }
    if (node.isArray()) {
      List<String> names = node.findValuesAsString("firstName", new ArrayList<>());
      if (!names.isEmpty()) {
        return String.join(", ", names);
      }
      return node.toString();
    }
    return node.asText();
  }

  private static String getDisplayName(String fieldName) {
    return FIELD_DISPLAY_NAMES.getOrDefault(fieldName, fieldName);
  }

  private static Set<String> getSignificantFields(String entityType) {
    return ENTITY_SIGNIFICANT_FIELDS.getOrDefault(entityType, Set.of());
  }

  private static ImpactLevel computeImpact(List<ChangeDetail> details) {
    for (ChangeDetail detail : details) {
      if (detail.changeType() == ChangeType.CREATED
          || detail.changeType() == ChangeType.DELETED) return ImpactLevel.HIGH;
      if (STATUS_LIKE_FIELDS.contains(detail.fieldName())) return ImpactLevel.HIGH;
      if (ASSIGNMENT_LIKE_FIELDS.contains(detail.fieldName())) return ImpactLevel.HIGH;
    }
    return ImpactLevel.MEDIUM;
  }

  public String formatSummary(List<EntityChangeGroup> groups) {
    if (groups == null || groups.isEmpty()) {
      return "During this meeting, the following changes were made:\n\nNo changes were made during this meeting.\n";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("During this meeting, the following changes were made:\n\n");

    for (EntityChangeGroup group : groups) {
      sb.append("### ").append(group.entityType()).append("\n");
      for (CategorizedChange change : group.changes()) {
        String title = extractTitle(change.details());
        if (title == null) {
          title = change.identifier();
        }
        sb.append("- **").append(change.changeType().name()).append("**");
        if (title != null) {
          sb.append(": \"").append(title).append("\"");
        }
        sb.append("\n");
        for (ChangeDetail detail : change.details()) {
          switch (detail.changeType()) {
            case CREATED ->
              sb.append("  - ").append(detail.displayName())
                  .append(": ").append(detail.newValue()).append("\n");
            case DELETED ->
              sb.append("  - ").append(detail.displayName())
                  .append(" was: ").append(detail.oldValue()).append("\n");
            case UPDATED -> {
              if ("isArchived".equals(detail.fieldName())) {
                sb.append("  - ").append("true".equals(String.valueOf(detail.newValue()))
                    ? "Archived" : "Unarchived").append("\n");
              } else {
                sb.append("  - ").append(detail.displayName())
                    .append(": ").append(detail.oldValue())
                    .append(" → ").append(detail.newValue()).append("\n");
              }
            }
          }
        }
      }
      sb.append("\n");
    }

    return sb.toString();
  }

  private static String extractTitle(List<ChangeDetail> details) {
    return details.stream()
        .filter(d -> "title".equals(d.fieldName()) || "name".equals(d.fieldName()))
        .findFirst()
        .map(d -> d.changeType() == ChangeType.DELETED
            ? String.valueOf(d.oldValue()) : String.valueOf(d.newValue()))
        .orElse(null);
  }
}
