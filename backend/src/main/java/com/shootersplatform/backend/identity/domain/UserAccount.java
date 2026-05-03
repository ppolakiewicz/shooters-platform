package com.shootersplatform.backend.identity.domain;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserAccount(
        UUID id,
        EmailAddress email,
        String passwordHash,
        boolean enabled,
        Set<UserRole> roles,
        Instant createdAt,
        Instant updatedAt
) {

    public static UserAccount register(UUID id, EmailAddress email, String passwordHash, Instant now) {
        return new UserAccount(id, email, passwordHash, true, Set.of(UserRole.USER), now, now);
    }

    public UserAccount {
        roles = Set.copyOf(roles);
    }
}
