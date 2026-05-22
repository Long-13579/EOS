package com.ces.eos.repository;

import com.ces.eos.entity.L10Meeting;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface L10MeetingRepository extends JpaRepository<L10Meeting, UUID> {
  Optional<L10Meeting> findByTeam_IdAndWeekStartDate(UUID teamId, LocalDate weekStartDate);

  boolean existsByIdAndTeam_Users_Id(UUID meetingId, UUID userId);

  @Query(
      "SELECT m FROM L10Meeting m "
          + "LEFT JOIN FETCH m.team "
          + "LEFT JOIN FETCH m.facilitator "
          + "LEFT JOIN FETCH m.scribe "
          + "LEFT JOIN FETCH m.createdBy "
          + "LEFT JOIN FETCH m.updatedBy "
          + "WHERE m.id = :meetingId")
  Optional<L10Meeting> findByIdWithRelations(@Param("meetingId") UUID meetingId);
}
