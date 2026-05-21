package com.ces.eos.entity;

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
@Table(name = "issues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Issue {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @Column(nullable = false)
  String title;

  String description;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "issue_type_id")
  IssueType issueType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", insertable = false, updatable = false)
  User creator;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id", nullable = false)
  Team team;

  @Column(name = "is_archived", nullable = false)
  @Builder.Default
  Boolean isArchived = false;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  Instant updatedAt;

  @CreatedBy
  @Column(name = "created_by")
  UUID createdBy;

  @LastModifiedBy
  @Column(name = "updated_by")
  UUID updatedBy;
}
