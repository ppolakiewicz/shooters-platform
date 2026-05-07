package com.shootersplatform.backend.identity.domain;

import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class IdentityService {

    private final UserAccountRepository userAccounts;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;
    private final Clock clock;

    public IdentityService(
            UserAccountRepository userAccounts,
            PasswordHasher passwordHasher,
            Clock clock
    ) {
        this.userAccounts = userAccounts;
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = new PasswordPolicy();
        this.clock = clock;
    }

    public UserAccount register(String rawEmail, String password) {
        EmailAddress email = new EmailAddress(rawEmail);
        passwordPolicy.validate(password);

        if (userAccounts.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        UserAccount account = UserAccount.register(UserId.newId(), email, passwordHasher.hash(password), clock.instant());
        return userAccounts.save(account);
    }

    public UserAccount authenticate(EmailAddress email, String password) {
        return userAccounts.findByEmail(email)
                .filter(account -> account.enabled() && passwordHasher.matches(password, account.passwordHash()))
                .orElseThrow(InvalidCredentialsException::new);
    }
}
