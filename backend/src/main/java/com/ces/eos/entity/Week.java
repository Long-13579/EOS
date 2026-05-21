package com.ces.eos.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "weeks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Week {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @Column(name = "start_date", nullable = false, unique = true)
  LocalDate startDate;

  @Column(name = "end_date", nullable = false, unique = true)
  LocalDate endDate;
}
