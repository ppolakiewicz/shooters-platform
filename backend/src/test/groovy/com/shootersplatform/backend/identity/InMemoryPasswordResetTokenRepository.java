package com.shootersplatform.backend.identity;

import com.shootersplatform.backend.identity.domain.PasswordResetToken;
import com.shootersplatform.backend.identity.domain.PasswordResetTokenRepository;
import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@NullMarked
public class InMemoryPasswordResetTokenRepository implements PasswordResetTokenRepository {

    private final Map<UUID, PasswordResetToken> byId = new HashMap<>();
    private final Map<String, PasswordResetToken> byHash = new HashMap<>();

    @Override
    public void markActiveTokensUsed(UserId userId, Instant now) {
        byId.values().stream()
                .filter(token -> token.userId().equals(userId))
                .filter(token -> token.usedAt() == null)
                .filter(token -> token.expiresAt().isAfter(now))
                .map(token -> token.markUsed(now))
                .toList()
                .forEach(this::save);
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return Optional.ofNullable(byHash.get(tokenHash));
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        byId.put(token.id(), token);
        byHash.put(token.tokenHash(), token);
        return token;
    }

    public int count() {
        return byId.size();
    }
}
