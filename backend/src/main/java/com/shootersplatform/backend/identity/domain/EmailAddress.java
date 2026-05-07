package com.shootersplatform.backend.identity.domain;

import java.io.Serializable;
import java.util.Locale;
import java.util.regex.Pattern;

public record EmailAddress(String value) implements Serializable {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public EmailAddress {
        if (value == null) {
            throw new IdentityValidationException("Email is required");
        }

        value = value.trim().toLowerCase(Locale.ROOT);

        if (value.isBlank()) {
            throw new IdentityValidationException("Email is required");
        }

        if (value.length() > 320 || !EMAIL_PATTERN.matcher(value).matches()) {
            throw new IdentityValidationException("Email is invalid");
        }
    }
}
