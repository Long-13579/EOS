package com.ces.eos.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Team {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @Column(nullable = false, unique = true)
  String name;

  @Column(name = "is_leadership", nullable = false)
  @Builder.Default
  Boolean isLeadership = false;

  @ManyToMany(mappedBy = "teams", fetch = FetchType.LAZY)
  @Builder.Default
  Set<User> users = new HashSet<>();

  @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
  @Builder.Default
  List<Todo> todos = new ArrayList<>();

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
