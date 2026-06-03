package com.ces.eos.entity;

import com.ces.eos.enums.AiSummaryStatus;
import com.ces.eos.enums.L10MeetingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "l10_meetings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class L10Meeting {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id", nullable = false)
  Team team;

  @Column(name = "meeting_date", nullable = false)
  LocalDate meetingDate;

  @Column(name = "meeting_time", nullable = false)
  LocalTime meetingTime;

  @Column(name = "week_start_date", nullable = false)
  LocalDate weekStartDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facilitator_id", nullable = false)
  User facilitator;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "scribe_id", nullable = false)
  User scribe;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  L10MeetingStatus status = L10MeetingStatus.SCHEDULED;

  @Column(name = "conclude_key_decisions")
  String concludeKeyDecisions;

  @Column(name = "conclude_cascading_message")
  String concludeCascadingMessage;

  @OneToMany(mappedBy = "meeting", fetch = FetchType.LAZY)
  @Builder.Default
  List<L10MeetingRating> ratings = new ArrayList<>();

  @Column(name = "ai_summary")
  String aiSummary;

  @Enumerated(EnumType.STRING)
  @Column(name = "ai_summary_status", length = 16)
  AiSummaryStatus aiSummaryStatus;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  Instant updatedAt;

  @CreatedBy
  @Column(name = "created_by")
  UUID createdById;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", insertable = false, updatable = false)
  User createdBy;

  @LastModifiedBy
  @Column(name = "updated_by")
  UUID updatedById;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by", insertable = false, updatable = false)
  User updatedBy;
}
