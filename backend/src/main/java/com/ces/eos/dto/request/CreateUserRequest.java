package com.ces.eos.dto.request;

import com.ces.eos.annotation.ValueOfEnum;
import com.ces.eos.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record CreateUserRequest(
    @NotBlank(message = "First name cannot be blank")
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        @Pattern(
            regexp = "^[a-zA-ZÀ-ỹ]+(?:[ \\-\\'][a-zA-ZÀ-ỹ]+)*$",
            message =
                "First name must not contain leading or trailing whitespace or special characters")
        String firstName,
    @NotBlank(message = "Last name cannot be blank")
        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        @Pattern(
            regexp = "^[a-zA-ZÀ-ỹ]+(?:[ \\-\\'][a-zA-ZÀ-ỹ]+)*$",
            message =
                "Last name must not contain leading or trailing whitespace or special characters")
        String lastName,
    @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email must be a valid email address")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,
    @NotBlank(message = "Role cannot be blank")
        @ValueOfEnum(enumClass = UserRole.class, message = "Role must be one of {acceptedValues}")
        String role,
    Set<UUID> teamIds) {}
