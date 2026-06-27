package com.ces.eos.service;

import java.util.UUID;

public interface UserDeactivationValidator {

  void validate(UUID userId);
}
