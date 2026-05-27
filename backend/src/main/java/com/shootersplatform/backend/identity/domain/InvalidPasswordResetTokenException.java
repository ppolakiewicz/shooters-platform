package com.shootersplatform.backend.identity.domain;

public class InvalidPasswordResetTokenException extends RuntimeException {

    public InvalidPasswordResetTokenException() {
        super("Password reset link is invalid or expired");
    }
}
