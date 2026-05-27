package com.shootersplatform.backend.identity.domain;

public interface PasswordResetTokenGenerator {

    String generateToken();

    String hash(String token);
}
