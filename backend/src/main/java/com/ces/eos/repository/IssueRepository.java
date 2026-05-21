package com.ces.eos.repository;

import com.ces.eos.entity.Issue;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID> {

  @Query(
      value =
          "SELECT i.id FROM Issue i " + "WHERE i.team.id = :teamId AND i.isArchived = :isArchived")
  Page<UUID> findIssueIdsByTeamId(
      @Param("teamId") UUID teamId, @Param("isArchived") Boolean isArchived, Pageable pageable);

  @Query(
      value =
          "SELECT i.id FROM Issue i WHERE i.team.id = :teamId AND i.issueType.id = :issueTypeId AND"
              + " i.isArchived = :isArchived")
  Page<UUID> findIssueIdsByTeamIdAndIssueTypeId(
      @Param("teamId") UUID teamId,
      @Param("issueTypeId") UUID issueTypeId,
      @Param("isArchived") Boolean isArchived,
      Pageable pageable);

  @Query(
      value =
          "SELECT i.id FROM Issue i "
              + "WHERE i.team.id = :teamId AND i.issueType IS NULL AND i.isArchived = :isArchived")
  Page<UUID> findIssueIdsByTeamIdAndIssueTypeIsNull(
      @Param("teamId") UUID teamId, @Param("isArchived") Boolean isArchived, Pageable pageable);

  @Query(
      value =
          "SELECT i.id FROM Issue i "
              + "LEFT JOIN i.issueType issueType "
              + "WHERE i.team.id = :teamId "
              + "AND i.isArchived = :isArchived "
              + "AND (issueType IS NULL OR issueType.id <> :excludeTypeId)")
  Page<UUID> findIssueIdsByTeamIdExcludingIssueTypeId(
      @Param("teamId") UUID teamId,
      @Param("isArchived") Boolean isArchived,
      @Param("excludeTypeId") UUID excludeTypeId,
      Pageable pageable);

  @Query(
      value =
          "SELECT DISTINCT i FROM Issue i "
              + "JOIN FETCH i.team "
              + "LEFT JOIN FETCH i.issueType "
              + "LEFT JOIN FETCH i.creator "
              + "WHERE i.id IN :ids")
  List<Issue> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query(
      "SELECT i FROM Issue i "
          + "JOIN FETCH i.team "
          + "LEFT JOIN FETCH i.issueType "
          + "LEFT JOIN FETCH i.creator "
          + "WHERE i.id = :id")
  Optional<Issue> findById(@Param("id") UUID id);

  boolean existsByIdAndTeam_Users_Id(UUID issueId, UUID userId);
}
