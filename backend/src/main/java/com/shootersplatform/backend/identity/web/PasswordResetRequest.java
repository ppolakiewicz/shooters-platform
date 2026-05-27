package com.shootersplatform.backend.identity.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

record PasswordResetRequest(
        @NotBlank @Email String email
) {
}
