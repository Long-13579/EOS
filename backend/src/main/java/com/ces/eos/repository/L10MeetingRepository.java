package com.ces.eos.repository;

import com.ces.eos.entity.L10Meeting;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface L10MeetingRepository extends JpaRepository<L10Meeting, UUID> {
  Optional<L10Meeting> findByTeam_IdAndWeekStartDate(UUID teamId, LocalDate weekStartDate);
}
