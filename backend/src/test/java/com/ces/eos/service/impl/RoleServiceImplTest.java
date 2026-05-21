package com.ces.eos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ces.eos.entity.Role;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.enums.UserRole;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.repository.RoleRepository;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

  @Mock private RoleRepository roleRepository;

  @InjectMocks private RoleServiceImpl roleService;

  @Nested
  class GetRoleByName {

    @Test
    void getRoleByName_caseInsensitiveNameAndRoleExists_returnsRole() {
      Role adminRole = role();
      when(roleRepository.findByName(UserRole.ADMIN)).thenReturn(Optional.of(adminRole));

      Role result = roleService.getRoleByName("admin");

      assertThat(result).isEqualTo(adminRole);
      verify(roleRepository).findByName(UserRole.ADMIN);
    }

    @Test
    void getRoleByName_invalidRoleName_throwsResourceNotFoundException() {
      assertThatThrownBy(() -> roleService.getRoleByName("owner"))
          .isInstanceOf(ResourceNotFoundException.class)
          .satisfies(
              ex ->
                  assertThat(((ResourceNotFoundException) ex).getErrorCode())
                      .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void getRoleByName_validNameButRoleMissing_throwsIllegalStateException() {
      when(roleRepository.findByName(UserRole.ADMIN)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> roleService.getRoleByName("ADMIN"))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Required role is not configured in the system: ADMIN");

      verify(roleRepository).findByName(UserRole.ADMIN);
    }
  }

  private static Role role() {
    return mock(Role.class);
  }
}
