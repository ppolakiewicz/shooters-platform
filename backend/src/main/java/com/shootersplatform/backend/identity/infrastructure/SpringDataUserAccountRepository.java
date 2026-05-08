package com.shootersplatform.backend.identity.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataUserAccountRepository extends JpaRepository<UserAccountEntity, UUID> {

    boolean existsByEmail(String email);

    boolean existsByUsernameIgnoreCase(String username);

    Optional<UserAccountEntity> findByEmail(String email);
}
