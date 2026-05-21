package com.ces.eos.service.impl;

import com.ces.eos.entity.Role;
import com.ces.eos.enums.UserRole;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.repository.RoleRepository;
import com.ces.eos.service.RoleService;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

  private final RoleRepository roleRepository;

  @Override
  public Role getRoleByName(String roleName) {
    log.debug("action=getRoleByName.start roleName={}", roleName);
    log.debug("action=getRoleByName.branch.resolveEnum roleName={}", roleName);
    UserRole userRole =
        Arrays.stream(UserRole.values())
            .filter(role -> role.name().equalsIgnoreCase(roleName))
            .findFirst()
            .orElseThrow(
                () -> {
                  log.warn(
                      "action=getRoleByName.validationFailed reason=invalidRoleName roleName={}",
                      roleName);
                  return new ResourceNotFoundException("Invalid role name: " + roleName);
                });

    log.debug("action=getRoleByName.repo.findByName role={}", userRole);
    Role role =
        roleRepository
            .findByName(userRole)
            .orElseThrow(
                () -> {
                  log.error(
                      "action=getRoleByName.validationFailed reason=roleMissingInDb roleName={}",
                      roleName);
                  return new IllegalStateException(
                      String.format("Required role is not configured in the system: %s", userRole));
                });
    log.debug("action=getRoleByName.success roleId={}", role.getId());
    return role;
  }
}
