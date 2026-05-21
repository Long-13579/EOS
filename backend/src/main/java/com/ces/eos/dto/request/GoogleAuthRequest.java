package com.ces.eos.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(
    @NotBlank(message = "Authorization code is required") String code,
    @NotBlank(message = "Redirect URI is required") String redirectUri) {}
