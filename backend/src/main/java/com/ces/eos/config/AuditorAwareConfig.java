package com.ces.eos.config;

import com.ces.eos.security.CustomUserDetails;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuditorAwareConfig implements AuditorAware<UUID> {
  @Override
  public Optional<UUID> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof CustomUserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) principal;
      return Optional.of(userDetails.getId());
    }

    return Optional.empty();
  }
}
