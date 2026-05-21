package com.ces.eos.repository;

import com.ces.eos.entity.Role;
import com.ces.eos.enums.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
  Optional<Role> findByName(UserRole name);
}
