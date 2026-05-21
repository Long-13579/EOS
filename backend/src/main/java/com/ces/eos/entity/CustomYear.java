package com.ces.eos.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "years")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class CustomYear {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @Min(1900)
  @Column(nullable = false, unique = true)
  int year;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  Instant createdAt;
}
