package com.shootersplatform.backend.identity.domain;

public interface PasswordHasher {

    String hash(String password);

    boolean matches(String password, String passwordHash);
}
