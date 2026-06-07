package com.ces.eos.repository;

import com.ces.eos.entity.Todo;
import com.ces.eos.enums.TodoStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends JpaRepository<Todo, UUID> {

  @Query(
      value =
          "SELECT t.id FROM Todo t "
              + "WHERE t.team.id = :teamId AND t.isArchived = :isArchived "
              + "ORDER BY "
              + "CASE WHEN t.status = com.ces.eos.enums.TodoStatus.COMPLETED THEN 1 "
              + "ELSE 0 END ASC, "
              + "t.dueDate ASC NULLS LAST, "
              + "t.createdAt DESC")
  Page<UUID> findTodoIdsByTeamIdAndArchiveStatus(
      @Param("teamId") UUID teamId, @Param("isArchived") boolean isArchived, Pageable pageable);

  @Query(
      value =
          "SELECT t.id FROM Todo t "
              + "WHERE t.team.id = :teamId AND t.status = :status AND t.isArchived = :isArchived")
  Page<UUID> findTodoIdsByTeamIdAndStatusAndArchiveStatus(
      @Param("teamId") UUID teamId,
      @Param("status") TodoStatus status,
      @Param("isArchived") boolean isArchived,
      Pageable pageable);

  @Query(
      value =
          "SELECT t.id FROM Todo t "
              + "WHERE t.team.id = :teamId AND t.issue.id = :issueId AND t.isArchived = :isArchived")
  Page<UUID> findTodoIdsByTeamIdAndIssueId(
      @Param("teamId") UUID teamId,
      @Param("issueId") UUID issueId,
      @Param("isArchived") boolean isArchived,
      Pageable pageable);

  @Query(
      value =
          "SELECT t.id FROM Todo t JOIN t.assignees assignee JOIN t.team.users teamUser"
              + " WHERE assignee.id = :assigneeId AND teamUser.id = :assigneeId AND t.isArchived = false"
              + " ORDER BY CASE WHEN t.status ="
              + " com.ces.eos.enums.TodoStatus.COMPLETED THEN 1 ELSE 0 END ASC, t.dueDate ASC"
              + " NULLS LAST, t.createdAt DESC")
  Page<UUID> findActiveTodoIdsByAssigneeId(@Param("assigneeId") UUID assigneeId, Pageable pageable);

  @Query(
      value =
          "SELECT DISTINCT t FROM Todo t "
              + "LEFT JOIN FETCH t.team "
              + "LEFT JOIN FETCH t.assignees "
              + "LEFT JOIN FETCH t.issue "
              + "WHERE t.id IN :ids")
  List<Todo> findAllByIdIn(@Param("ids") List<UUID> ids);

  boolean existsByIdAndTeam_Users_Id(UUID todoId, UUID userId);

  @Query(
      value =
          "SELECT t.issue.id, COUNT(t) FROM Todo t "
              + "WHERE t.issue.id IN :issueIds GROUP BY t.issue.id")
  List<Object[]> countTodosByIssueIds(@Param("issueIds") List<UUID> issueIds);

  @Query(
      """
      SELECT t.title FROM Todo t JOIN t.assignees a
      WHERE a.id = :userId AND t.team.id = :teamId AND t.isArchived = false
      """)
  List<String> findActiveTitlesByAssigneeIdAndTeamId(
      @Param("userId") UUID userId, @Param("teamId") UUID teamId);

  @Modifying
  @Query("DELETE FROM Todo t WHERE t.team.id = :teamId")
  void deleteAllByTeamId(@Param("teamId") UUID teamId);
}
