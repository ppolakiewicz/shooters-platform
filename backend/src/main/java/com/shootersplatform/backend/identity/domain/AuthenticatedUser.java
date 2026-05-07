package com.shootersplatform.backend.identity.domain;

import java.io.Serializable;
import java.util.Set;

public record AuthenticatedUser(UserId id, EmailAddress email, Set<UserRole> roles) implements Serializable {

    public static AuthenticatedUser from(UserAccount account) {
        return new AuthenticatedUser(account.id(), account.email(), account.roles());
    }

    public AuthenticatedUser {
        roles = Set.copyOf(roles);
    }
}
