package com.shootersplatform.backend.identity.domain;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record PasswordResetToken(
        UUID id,
        UserId userId,
        String tokenHash,
        Instant expiresAt,
        @Nullable Instant usedAt,
        Instant createdAt
) {

    public static PasswordResetToken create(UserId userId, String tokenHash, Instant expiresAt, Instant now) {
        return new PasswordResetToken(UUID.randomUUID(), userId, tokenHash, expiresAt, null, now);
    }

    public boolean canBeUsedAt(Instant now) {
        return usedAt == null && !now.isAfter(expiresAt);
    }

    public PasswordResetToken markUsed(Instant now) {
        return new PasswordResetToken(id, userId, tokenHash, expiresAt, now, createdAt);
    }
}
