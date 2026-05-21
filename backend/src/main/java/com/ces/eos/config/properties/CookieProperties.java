package com.ces.eos.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cookie")
public record CookieProperties(
    @NotNull(message = "Cookie secure flag is required") Boolean secure,
    @NotNull(message = "Cookie httpOnly flag is required") Boolean httpOnly,
    @NotBlank(message = "Cookie SameSite attribute is required") String sameSite) {}
