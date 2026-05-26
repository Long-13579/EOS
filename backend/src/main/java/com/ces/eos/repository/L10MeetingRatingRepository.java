package com.ces.eos.repository;

import com.ces.eos.entity.L10MeetingRating;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface L10MeetingRatingRepository extends JpaRepository<L10MeetingRating, UUID> {
  Optional<L10MeetingRating> findByMeeting_IdAndMember_Id(UUID meetingId, UUID memberId);

  List<L10MeetingRating> findByMeeting_Id(UUID meetingId);
}
