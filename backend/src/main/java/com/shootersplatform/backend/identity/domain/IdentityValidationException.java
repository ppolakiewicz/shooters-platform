package com.shootersplatform.backend.identity.domain;

public class IdentityValidationException extends RuntimeException {

    public IdentityValidationException(String message) {
        super(message);
    }
}
