package com.ces.eos.repository;

import com.ces.eos.entity.MetricValue;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricValueRepository extends JpaRepository<MetricValue, UUID> {

  @Query("SELECT mv FROM MetricValue mv WHERE mv.metric.id = :metricId AND mv.week.id = :weekId")
  Optional<MetricValue> findByMetricIdAndWeekId(
      @Param("metricId") UUID metricId, @Param("weekId") UUID weekId);

  @Modifying(clearAutomatically = true)
  @Query(
      value =
          """
          INSERT INTO metric_values (id, metric_id, week_id, created_at)
          VALUES (gen_random_uuid(), :metricId, :weekId, NOW())
          ON CONFLICT (metric_id, week_id) DO NOTHING
          """,
      nativeQuery = true)
  void insertIfNotExists(@Param("metricId") UUID metricId, @Param("weekId") UUID weekId);

  @Query(
      """
      SELECT mv FROM MetricValue mv
      JOIN FETCH mv.metric
      JOIN FETCH mv.week
      LEFT JOIN FETCH mv.updatedBy
      WHERE mv.metric.id IN :metricIds
      AND mv.week.id IN :weekIds
      """)
  List<MetricValue> findByMetricIdsAndWeekIds(
      @Param("metricIds") List<UUID> metricIds, @Param("weekIds") List<UUID> weekIds);
}
