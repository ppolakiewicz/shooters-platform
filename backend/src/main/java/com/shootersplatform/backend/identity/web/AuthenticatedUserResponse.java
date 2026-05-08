package com.shootersplatform.backend.identity.web;

import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import com.shootersplatform.backend.identity.domain.UserRole;

import java.util.Set;
import java.util.UUID;

record AuthenticatedUserResponse(UUID id, String email, String username, Set<UserRole> roles) {

    static AuthenticatedUserResponse from(AuthenticatedUser user) {
        return new AuthenticatedUserResponse(
                user.id().value(),
                user.email().value(),
                user.username().value(),
                user.roles()
        );
    }

    AuthenticatedUserResponse {
        roles = Set.copyOf(roles);
    }
}
