package com.ces.eos.entity;

import com.ces.eos.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @Enumerated(EnumType.STRING)
  @Column(unique = true, nullable = false)
  UserRole name;
}
