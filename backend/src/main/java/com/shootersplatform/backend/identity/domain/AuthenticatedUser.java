package com.shootersplatform.backend.identity.domain;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(UUID id, String email, Set<String> roles) implements Serializable {

    public static AuthenticatedUser from(UserAccount account) {
        return new AuthenticatedUser(
                account.id(),
                account.email().value(),
                account.roles().stream().map(UserRole::name).collect(java.util.stream.Collectors.toUnmodifiableSet())
        );
    }
}
