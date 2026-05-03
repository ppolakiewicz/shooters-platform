package com.shootersplatform.backend.identity.domain;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository {

    boolean existsByEmail(EmailAddress email);

    Optional<UserAccount> findByEmail(EmailAddress email);

    Optional<UserAccount> findById(UUID id);

    UserAccount save(UserAccount userAccount);
}
