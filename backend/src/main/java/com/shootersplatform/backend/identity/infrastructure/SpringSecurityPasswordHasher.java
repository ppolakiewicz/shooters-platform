package com.shootersplatform.backend.identity.infrastructure;

import com.shootersplatform.backend.identity.domain.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class SpringSecurityPasswordHasher implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    SpringSecurityPasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean matches(String password, String passwordHash) {
        return passwordEncoder.matches(password, passwordHash);
    }
}
