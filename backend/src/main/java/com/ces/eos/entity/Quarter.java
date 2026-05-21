package com.ces.eos.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.MonthDay;
import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "quarters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Quarter {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @Column(nullable = false, unique = true, length = 255)
  String name;

  @Column(name = "start_date", nullable = false)
  MonthDay startDate;

  @Column(name = "end_date", nullable = false)
  MonthDay endDate;

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
