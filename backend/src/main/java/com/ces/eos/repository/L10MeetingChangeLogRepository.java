package com.ces.eos.repository;

import com.ces.eos.entity.L10MeetingChangeLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface L10MeetingChangeLogRepository extends JpaRepository<L10MeetingChangeLog, UUID> {

  List<L10MeetingChangeLog> findByMeeting_Id(UUID meetingId);

  Page<L10MeetingChangeLog> findByMeeting_Id(UUID meetingId, Pageable pageable);

  @Query("SELECT log FROM L10MeetingChangeLog log WHERE log.meeting.id = :meetingId ORDER BY log.createdAt ASC")
  List<L10MeetingChangeLog> findByMeetingIdOrderedByCreatedAt(@Param("meetingId") UUID meetingId);
}
