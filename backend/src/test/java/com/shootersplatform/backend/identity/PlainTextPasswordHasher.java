package com.shootersplatform.backend.identity;

import com.shootersplatform.backend.identity.domain.PasswordHasher;

public class PlainTextPasswordHasher implements PasswordHasher {

    @Override
    public String hash(String password) {
        return "hashed:" + password;
    }

    @Override
    public boolean matches(String password, String passwordHash) {
        return hash(password).equals(passwordHash);
    }
}
