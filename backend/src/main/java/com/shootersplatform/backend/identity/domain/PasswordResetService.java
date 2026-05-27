package com.shootersplatform.backend.identity.domain;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class PasswordResetService {

    private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(30);

    private final UserAccountRepository userAccounts;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordResetTokenGenerator tokenGenerator;
    private final PasswordResetNotificationGateway notifications;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;
    private final Clock clock;

    public PasswordResetService(
            UserAccountRepository userAccounts,
            PasswordResetTokenRepository resetTokens,
            PasswordResetTokenGenerator tokenGenerator,
            PasswordResetNotificationGateway notifications,
            PasswordHasher passwordHasher,
            Clock clock
    ) {
        this.userAccounts = userAccounts;
        this.resetTokens = resetTokens;
        this.tokenGenerator = tokenGenerator;
        this.notifications = notifications;
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = new PasswordPolicy();
        this.clock = clock;
    }

    public void requestReset(String rawEmail, String frontendBaseUrl) {
        EmailAddress email = new EmailAddress(rawEmail);
        userAccounts.findByEmail(email)
                .filter(UserAccount::enabled)
                .ifPresent(account -> createResetToken(account, frontendBaseUrl));
    }

    public void resetPassword(String rawToken, String newPassword) {
        passwordPolicy.validate(newPassword);
        Instant now = clock.instant();
        PasswordResetToken token = resetTokens.findByTokenHash(tokenGenerator.hash(rawToken))
                .filter(candidate -> candidate.canBeUsedAt(now))
                .orElseThrow(InvalidPasswordResetTokenException::new);

        UserAccount account = userAccounts.findById(token.userId())
                .filter(UserAccount::enabled)
                .orElseThrow(InvalidPasswordResetTokenException::new);

        userAccounts.save(account.changePassword(passwordHasher.hash(newPassword), now));
        resetTokens.save(token.markUsed(now));
    }

    private void createResetToken(UserAccount account, String frontendBaseUrl) {
        Instant now = clock.instant();
        resetTokens.markActiveTokensUsed(account.id(), now);

        String rawToken = tokenGenerator.generateToken();
        PasswordResetToken token = PasswordResetToken.create(
                account.id(),
                tokenGenerator.hash(rawToken),
                now.plus(TOKEN_LIFETIME),
                now
        );
        resetTokens.save(token);
        notifications.send(new PasswordResetNotification(account.email(), resetLink(frontendBaseUrl, rawToken)));
    }

    private static String resetLink(String frontendBaseUrl, String rawToken) {
        String normalizedBaseUrl = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        return normalizedBaseUrl + "/reset-password/" + rawToken;
    }
}
