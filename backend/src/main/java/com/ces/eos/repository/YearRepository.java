package com.ces.eos.repository;

import com.ces.eos.entity.CustomYear;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface YearRepository extends JpaRepository<CustomYear, UUID> {
  List<CustomYear> findAllByOrderByYearAsc();

  Optional<CustomYear> findByYear(Integer year);

  @Modifying(clearAutomatically = true)
  @Query(
      value =
          "INSERT INTO years (id, year) "
              + "VALUES (gen_random_uuid(), :year) "
              + "ON CONFLICT (year) DO NOTHING",
      nativeQuery = true)
  void insertIfNotExists(@Param("year") Integer year);
}
