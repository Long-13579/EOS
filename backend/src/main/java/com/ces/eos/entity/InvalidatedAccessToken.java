package com.ces.eos.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "invalidated_access_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvalidatedAccessToken {

  @Id
  @Column(updatable = false, length = 255)
  String jti;

  @Column(name = "expires_at", nullable = false)
  Instant expiresAt;
}
