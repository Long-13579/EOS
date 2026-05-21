package com.ces.eos.repository;

import com.ces.eos.entity.Team;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
  boolean existsById(UUID id);

  boolean existsByNameIgnoreCase(String name);

  Set<Team> findAllByIdIn(Set<UUID> ids);

  List<Team> findAllByUsers_Id(UUID userId, Sort sort);
}
