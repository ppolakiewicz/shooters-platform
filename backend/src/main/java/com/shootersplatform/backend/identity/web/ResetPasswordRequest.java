package com.shootersplatform.backend.identity.web;

import jakarta.validation.constraints.NotBlank;

record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank String password
) {
}
