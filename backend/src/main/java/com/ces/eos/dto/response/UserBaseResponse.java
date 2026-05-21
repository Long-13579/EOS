package com.ces.eos.dto.response;

import java.util.UUID;

public record UserBaseResponse(UUID id, String firstName, String lastName, String email) {}
