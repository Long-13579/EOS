package com.ces.eos.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "issue_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IssueType {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @Column(nullable = false)
  String name;
}
