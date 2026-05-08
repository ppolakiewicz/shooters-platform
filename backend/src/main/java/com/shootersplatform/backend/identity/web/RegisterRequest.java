package com.shootersplatform.backend.identity.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 3, max = 32) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String username,
        @NotBlank String password
) {
}
