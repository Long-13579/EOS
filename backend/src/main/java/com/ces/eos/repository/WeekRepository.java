package com.ces.eos.repository;

import com.ces.eos.entity.Week;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WeekRepository extends JpaRepository<Week, UUID> {

  @Query(
      """
      SELECT w FROM Week w
      WHERE w.startDate <= :currentDate
      """)
  List<Week> findByStartDateLessThanEqual(
      @Param("currentDate") LocalDate currentDate, Pageable pageable);

  @Modifying(clearAutomatically = true)
  @Query(
      value =
          """
          INSERT INTO weeks (id, start_date, end_date)
          VALUES (gen_random_uuid(), :startDate, :endDate)
          ON CONFLICT DO NOTHING
          """,
      nativeQuery = true)
  void insertWeekIfNotExists(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  @Query("SELECT w FROM Week w WHERE w.startDate <= CURRENT_DATE AND w.endDate >= CURRENT_DATE")
  Optional<Week> findCurrentWeek();

  Optional<Week> findTopByStartDateLessThanOrderByStartDateDesc(LocalDate currentWeekStartDate);
}
