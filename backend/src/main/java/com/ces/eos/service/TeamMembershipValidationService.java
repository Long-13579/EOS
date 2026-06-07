package com.ces.eos.service;

import java.util.UUID;

public interface TeamMembershipValidationService {

  void validateUserTeamRemoval(UUID userId, UUID teamId);
}
