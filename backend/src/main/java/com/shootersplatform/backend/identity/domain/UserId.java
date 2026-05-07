package com.shootersplatform.backend.identity.domain;

import java.io.Serializable;
import java.util.UUID;

public record UserId(UUID value) implements Serializable {

    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }

    public UserId {
        if (value == null) {
            throw new IdentityValidationException("User id is required");
        }
    }
}
