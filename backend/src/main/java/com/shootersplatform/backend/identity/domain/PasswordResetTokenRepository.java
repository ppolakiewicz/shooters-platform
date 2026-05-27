package com.shootersplatform.backend.identity.domain;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository {

    void markActiveTokensUsed(UserId userId, Instant now);

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    PasswordResetToken save(PasswordResetToken token);
}
