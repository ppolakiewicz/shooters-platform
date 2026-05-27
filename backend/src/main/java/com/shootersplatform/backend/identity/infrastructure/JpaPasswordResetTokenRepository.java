package com.shootersplatform.backend.identity.infrastructure;

import com.shootersplatform.backend.identity.domain.PasswordResetToken;
import com.shootersplatform.backend.identity.domain.PasswordResetTokenRepository;
import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@NullMarked
@Repository
class JpaPasswordResetTokenRepository implements PasswordResetTokenRepository {

    private final SpringDataPasswordResetTokenRepository resetTokens;

    JpaPasswordResetTokenRepository(SpringDataPasswordResetTokenRepository resetTokens) {
        this.resetTokens = resetTokens;
    }

    @Override
    public void markActiveTokensUsed(UserId userId, Instant now) {
        resetTokens.markActiveTokensUsed(userId.value(), now);
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return resetTokens.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
        entity.setId(token.id());
        entity.setUserAccountId(token.userId().value());
        entity.setTokenHash(token.tokenHash());
        entity.setExpiresAt(token.expiresAt());
        entity.setUsedAt(token.usedAt());
        entity.setCreatedAt(token.createdAt());
        return toDomain(resetTokens.save(entity));
    }

    private PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
        return new PasswordResetToken(
                entity.getId(),
                new UserId(entity.getUserAccountId()),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getUsedAt(),
                entity.getCreatedAt()
        );
    }
}
