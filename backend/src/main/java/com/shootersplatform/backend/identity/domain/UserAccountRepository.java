package com.shootersplatform.backend.identity.domain;

import java.util.Optional;

public interface UserAccountRepository {

    boolean existsByEmail(EmailAddress email);

    Optional<UserAccount> findByEmail(EmailAddress email);

    Optional<UserAccount> findById(UserId id);

    UserAccount save(UserAccount userAccount);
}
