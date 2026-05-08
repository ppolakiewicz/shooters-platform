package com.shootersplatform.backend.identity;

import com.shootersplatform.backend.identity.domain.EmailAddress;
import com.shootersplatform.backend.identity.domain.UserAccount;
import com.shootersplatform.backend.identity.domain.UserAccountRepository;
import com.shootersplatform.backend.identity.domain.UserId;
import com.shootersplatform.backend.identity.domain.Username;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@NullMarked
public class InMemoryUserAccountRepository implements UserAccountRepository {

    private final Map<UserId, UserAccount> byId = new HashMap<>();
    private final Map<String, UserAccount> byEmail = new HashMap<>();
    private final Map<String, UserAccount> byUsername = new HashMap<>();

    @Override
    public boolean existsByEmail(EmailAddress email) {
        return byEmail.containsKey(email.value());
    }

    @Override
    public boolean existsByUsername(Username username) {
        return byUsername.containsKey(username.normalized());
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
        byUsername.put(userAccount.username().normalized(), userAccount);
        return userAccount;
    }

    public UserAccount savedByEmail(String email) {
        return Objects.requireNonNull(byEmail.get(new EmailAddress(email).value()));
    }

    public UserAccount savedByUsername(String username) {
        return Objects.requireNonNull(byUsername.get(new Username(username).normalized()));
    }

    public int count() {
        return byId.size();
    }
}
