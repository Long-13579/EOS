package com.ces.eos.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "google")
public record GoogleOAuthProperties(@NotBlank String clientId, @NotBlank String clientSecret) {}
