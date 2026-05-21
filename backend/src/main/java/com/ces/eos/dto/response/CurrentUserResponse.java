package com.ces.eos.dto.response;

import java.util.UUID;

public record CurrentUserResponse(
    UUID id, String firstName, String lastName, String email, String role) {}
