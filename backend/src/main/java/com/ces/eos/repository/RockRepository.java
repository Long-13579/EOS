package com.ces.eos.repository;

import com.ces.eos.entity.Rock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RockRepository extends JpaRepository<Rock, UUID> {

  @Query(
      """
      SELECT r FROM Rock r
      JOIN FETCH r.team
      JOIN FETCH r.year
      JOIN FETCH r.quarter
      JOIN FETCH r.owner
      LEFT JOIN FETCH r.createdBy
      LEFT JOIN FETCH r.updatedBy
      WHERE r.team.id = :teamId
      AND r.year.id = :yearId
      AND r.quarter.id = :quarterId
      AND r.isArchived = :isArchived
      ORDER BY r.dueDate ASC, r.createdAt DESC
      """)
  List<Rock> findAllByTeamIdAndYearIdAndQuarterId(
      @Param("teamId") UUID teamId,
      @Param("yearId") UUID yearId,
      @Param("quarterId") UUID quarterId,
      @Param("isArchived") Boolean isArchived);

  @Query(
      """
      SELECT r FROM Rock r
      JOIN FETCH r.team
      JOIN FETCH r.year
      JOIN FETCH r.quarter
      JOIN FETCH r.owner
      LEFT JOIN FETCH r.createdBy
      LEFT JOIN FETCH r.updatedBy
      WHERE r.owner.id = :ownerId
        AND r.year.id = :yearId
        AND r.quarter.id = :quarterId
        AND r.isArchived = false
      ORDER BY r.dueDate ASC, r.createdAt DESC
      """)
  List<Rock> findActiveRocksByOwnerIdAndYearAndQuarter(
      @Param("ownerId") UUID ownerId,
      @Param("yearId") UUID yearId,
      @Param("quarterId") UUID quarterId);

  @Query(
      """
      SELECT r FROM Rock r
      JOIN FETCH r.team
      JOIN FETCH r.owner
      JOIN FETCH r.year
      JOIN FETCH r.quarter
      LEFT JOIN FETCH r.createdBy
      LEFT JOIN FETCH r.updatedBy
      WHERE r.id = :id
      """)
  Optional<Rock> findById(@Param("id") UUID id);

  boolean existsByIdAndTeam_Users_Id(UUID rockId, UUID userId);

  @Query(
      """
      SELECT r.title FROM Rock r
      WHERE r.owner.id = :userId AND r.team.id = :teamId AND r.isArchived = false
      """)
  List<String> findActiveTitlesByOwnerIdAndTeamId(
      @Param("userId") UUID userId, @Param("teamId") UUID teamId);

  @Modifying
  @Query("DELETE FROM Rock r WHERE r.team.id = :teamId")
  void deleteAllByTeamId(@Param("teamId") UUID teamId);
}
