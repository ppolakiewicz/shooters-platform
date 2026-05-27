package com.shootersplatform.backend.identity.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

interface SpringDataPasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update PasswordResetTokenEntity token
            set token.usedAt = :now
            where token.userAccountId = :userAccountId
              and token.usedAt is null
              and token.expiresAt > :now
            """)
    void markActiveTokensUsed(@Param("userAccountId") UUID userAccountId, @Param("now") Instant now);
}
