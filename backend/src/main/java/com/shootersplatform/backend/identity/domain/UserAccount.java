package com.shootersplatform.backend.identity.domain;

import java.time.Instant;
import java.util.Set;

public record UserAccount(
        UserId id,
        EmailAddress email,
        Username username,
        String passwordHash,
        boolean enabled,
        Set<UserRole> roles,
        Instant createdAt,
        Instant updatedAt
) {

    public static UserAccount register(UserId id, EmailAddress email, Username username, String passwordHash, Instant now) {
        return new UserAccount(id, email, username, passwordHash, true, Set.of(UserRole.USER), now, now);
    }

    public UserAccount {
        roles = Set.copyOf(roles);
    }

    public UserAccount changePassword(String newPasswordHash, Instant now) {
        return new UserAccount(id, email, username, newPasswordHash, enabled, roles, createdAt, now);
    }
}
