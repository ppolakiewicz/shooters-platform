package com.shootersplatform.backend.identity.domain;

import java.util.Locale;
import java.util.Set;

public final class PasswordPolicy {

    private static final int MIN_LENGTH = 12;
    private static final int MAX_LENGTH = 128;
    private static final Set<String> BLOCKLIST = Set.of(
            "password1234",
            "password12345",
            "admin12345678",
            "qwerty123456",
            "letmein123456",
            "shooters123456"
    );

    public void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new IdentityValidationException("Password is required");
        }

        if (password.length() < MIN_LENGTH) {
            throw new IdentityValidationException("Password must be at least 12 characters long");
        }

        if (password.length() > MAX_LENGTH) {
            throw new IdentityValidationException("Password must be at most 128 characters long");
        }

        if (BLOCKLIST.contains(password.toLowerCase(Locale.ROOT))) {
            throw new IdentityValidationException("Password is too common");
        }
    }
}
