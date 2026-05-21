package com.ces.eos.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "first_name", nullable = false)
    String firstName;

    @Column(name = "last_name", nullable = false)
    String lastName;

    @Column(nullable = false, unique = true)
    String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    Role role;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "team_users",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    @BatchSize(size = 20)
    @Builder.Default
    Set<Team> teams = new HashSet<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    Boolean isActive = true;

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
