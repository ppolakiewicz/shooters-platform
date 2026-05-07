package com.shootersplatform.backend.identity;

import com.shootersplatform.backend.identity.domain.EmailAddress;
import com.shootersplatform.backend.identity.domain.UserAccount;
import com.shootersplatform.backend.identity.domain.UserAccountRepository;
import com.shootersplatform.backend.identity.domain.UserId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserAccountRepository implements UserAccountRepository {

    private final Map<UserId, UserAccount> byId = new HashMap<>();
    private final Map<String, UserAccount> byEmail = new HashMap<>();

    @Override
    public boolean existsByEmail(EmailAddress email) {
        return byEmail.containsKey(email.value());
    }

    @Override
    public Optional<UserAccount> findByEmail(EmailAddress email) {
        return Optional.ofNullable(byEmail.get(email.value()));
    }

    @Override
    public Optional<UserAccount> findById(UserId id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public UserAccount save(UserAccount userAccount) {
        byId.put(userAccount.id(), userAccount);
        byEmail.put(userAccount.email().value(), userAccount);
        return userAccount;
    }

    public UserAccount savedByEmail(String email) {
        return byEmail.get(new EmailAddress(email).value());
    }

    public int count() {
        return byId.size();
    }
}
