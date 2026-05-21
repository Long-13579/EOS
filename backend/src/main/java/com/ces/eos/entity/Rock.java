package com.ces.eos.entity;

import com.ces.eos.enums.RockCategory;
import com.ces.eos.enums.RockStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "rocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Rock {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id", nullable = false)
  Team team;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "year_id", nullable = false)
  CustomYear year;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "quarter_id", nullable = false)
  Quarter quarter;

  @Column(nullable = false, length = 255)
  String title;

  @Column(nullable = false, length = 2000)
  String description;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  RockStatus status = RockStatus.ON_TRACK;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  RockCategory category = RockCategory.INDIVIDUAL;

  @Column(name = "due_date", nullable = false)
  Instant dueDate;

  @Builder.Default
  @Column(name = "is_archived", nullable = false)
  Boolean isArchived = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  User owner;

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
