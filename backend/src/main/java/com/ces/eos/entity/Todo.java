package com.ces.eos.entity;

import com.ces.eos.enums.TodoStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
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
@Table(name = "todos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Todo {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @Column(nullable = false)
  String title;

  String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  TodoStatus status;

  @Column(name = "due_date")
  Instant dueDate;

  @Column(name = "is_archived", nullable = false)
  @Builder.Default
  Boolean isArchived = false;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "todo_assignees",
      joinColumns = @JoinColumn(name = "todo_id"),
      inverseJoinColumns = @JoinColumn(name = "assignee_id"))
  @Builder.Default
  Set<User> assignees = new HashSet<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id", nullable = false)
  Team team;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "issue_id")
  Issue issue;

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
