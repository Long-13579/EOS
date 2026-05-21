package com.ces.eos.repository;

import com.ces.eos.entity.Metric;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricRepository extends JpaRepository<Metric, UUID> {
  boolean existsByIdAndTeam_Users_Id(UUID metricId, UUID userId);

  @Query(
      """
      SELECT m FROM Metric m
      JOIN FETCH m.team
      JOIN FETCH m.owner
      LEFT JOIN FETCH m.createdBy
      LEFT JOIN FETCH m.updatedBy
      WHERE m.id = :id
      """)
  Optional<Metric> findByIdWithTeam(@Param("id") UUID id);

  @Query(
      """
      SELECT m FROM Metric m
      JOIN FETCH m.team
      JOIN FETCH m.owner
      LEFT JOIN FETCH m.createdBy
      LEFT JOIN FETCH m.updatedBy
      WHERE m.team.id = :teamId
      ORDER BY m.createdAt ASC, m.id ASC
      """)
  List<Metric> findByTeamId(@Param("teamId") UUID teamId);

  @Query(
      """
      SELECT m FROM Metric m
      JOIN FETCH m.team
      JOIN FETCH m.owner
      LEFT JOIN FETCH m.createdBy
      LEFT JOIN FETCH m.updatedBy
      WHERE m.owner.id = :ownerId
      ORDER BY m.createdAt ASC, m.id ASC
      """)
  List<Metric> findByOwnerId(@Param("ownerId") UUID ownerId);
}
