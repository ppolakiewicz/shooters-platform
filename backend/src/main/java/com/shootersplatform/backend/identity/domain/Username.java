package com.shootersplatform.backend.identity.domain;

import java.io.Serializable;
import java.util.Locale;
import java.util.regex.Pattern;

public record Username(String value) implements Serializable {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 32;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

    public Username {
        if (value == null) {
            throw new IdentityValidationException("Username is required");
        }

        value = value.trim();

        if (value.isBlank()) {
            throw new IdentityValidationException("Username is required");
        }

        if (value.length() < MIN_LENGTH) {
            throw new IdentityValidationException("Username must be at least 3 characters");
        }

        if (value.length() > MAX_LENGTH) {
            throw new IdentityValidationException("Username must be at most 32 characters");
        }

        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new IdentityValidationException("Username can contain only letters, numbers, underscores, and hyphens");
        }
    }

    public String normalized() {
        return value.toLowerCase(Locale.ROOT);
    }
}
