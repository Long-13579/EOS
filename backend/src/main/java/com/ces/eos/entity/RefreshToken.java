package com.ces.eos.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  User user;

  @Column(nullable = false, unique = true)
  String jti;

  @Column(name = "expires_at", nullable = false)
  Instant expiresAt;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  Instant createdAt;
}
