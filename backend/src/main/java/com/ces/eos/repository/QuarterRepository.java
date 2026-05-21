package com.ces.eos.repository;

import com.ces.eos.entity.Quarter;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuarterRepository extends JpaRepository<Quarter, UUID> {
  @Query(
      "SELECT q FROM Quarter q LEFT JOIN FETCH q.createdBy LEFT JOIN FETCH q.updatedBy ORDER BY"
          + " q.name ASC")
  List<Quarter> findAllByOrderByNameAsc();
}
