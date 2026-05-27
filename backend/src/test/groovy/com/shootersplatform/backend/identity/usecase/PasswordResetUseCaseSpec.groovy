package com.shootersplatform.backend.identity.usecase

import com.shootersplatform.backend.identity.*
import com.shootersplatform.backend.identity.domain.*
import spock.lang.Specification
import spock.util.time.MutableClock

import java.time.Instant
import java.time.ZoneOffset

class PasswordResetUseCaseSpec extends Specification {

    private InMemoryUserAccountRepository userAccounts
    private InMemoryPasswordResetTokenRepository resetTokens
    private InMemoryPasswordResetNotificationGateway notifications
    private InMemoryLoginRateLimiter rateLimiter
    private FixedPasswordResetTokenGenerator tokenGenerator
    private MutableClock clock
    private IdentityService identity
    private RequestPasswordResetUseCase requestPasswordReset
    private ResetPasswordUseCase resetPassword

    def setup() {
        userAccounts = new InMemoryUserAccountRepository()
        resetTokens = new InMemoryPasswordResetTokenRepository()
        notifications = new InMemoryPasswordResetNotificationGateway()
        rateLimiter = new InMemoryLoginRateLimiter()
        tokenGenerator = new FixedPasswordResetTokenGenerator("first-token", "second-token", "third-token")
        clock = new MutableClock(Instant.parse("2026-05-03T12:00:00Z"), ZoneOffset.UTC)
        identity = new IdentityService(userAccounts, new PlainTextPasswordHasher(), clock)
        def passwordReset = new PasswordResetService(
                userAccounts,
                resetTokens,
                tokenGenerator,
                notifications,
                new PlainTextPasswordHasher(),
                clock
        )
        requestPasswordReset = new RequestPasswordResetUseCase(passwordReset, rateLimiter, "http://localhost:4200")
        resetPassword = new ResetPasswordUseCase(passwordReset)
    }

    def "request emits reset link only for existing enabled account"() {
        given: "An enabled account exists"
            saveUser("owner@example.com", "OwnerOne", "hashed:correct horse battery")

        when: "A reset request uses different email casing"
            requestPasswordReset.request("OWNER@example.com", "127.0.0.1")

        then: "A single reset token is persisted and a link is sent"
            resetTokens.count() == 1
            notifications.sent()*.email()*.value() == ["owner@example.com"]
            notifications.sent()[0].resetLink() == "http://localhost:4200/reset-password/first-token"

        when: "A reset request is made for an unknown address"
            requestPasswordReset.request("unknown@example.com", "127.0.0.1")

        then: "No notification is sent for the unknown address"
            resetTokens.count() == 1
            notifications.sent().size() == 1
            rateLimiter.passwordResetAttempts() == 2
    }

    def "new reset request invalidates earlier active token"() {
        given: "An enabled account exists"
            saveUser("owner@example.com", "OwnerOne", "hashed:correct horse battery")

        when: "The user requests two reset links"
            requestPasswordReset.request("owner@example.com", "127.0.0.1")
            requestPasswordReset.request("owner@example.com", "127.0.0.1")

        then: "Two links were sent"
            notifications.sent()*.resetLink() == [
                    "http://localhost:4200/reset-password/first-token",
                    "http://localhost:4200/reset-password/second-token"
            ]

        when: "The first token is used"
            resetPassword.reset("first-token", "new correct password")

        then: "The first token can no longer reset the password"
            thrown(InvalidPasswordResetTokenException)

        when: "The second token is used"
            resetPassword.reset("second-token", "new correct password")

        then: "The password changes"
            identity.authenticate(new EmailAddress("owner@example.com"), "new correct password").email().value() == "owner@example.com"
    }

    def "reset token expires and is single use"() {
        given: "An enabled account has a reset link"
            saveUser("owner@example.com", "OwnerOne", "hashed:correct horse battery")
            requestPasswordReset.request("owner@example.com", "127.0.0.1")

        when: "The token is used once"
            resetPassword.reset("first-token", "new correct password")

        then: "The new password works and the old password does not"
            identity.authenticate(new EmailAddress("owner@example.com"), "new correct password")

        when: "The old password is used"
            identity.authenticate(new EmailAddress("owner@example.com"), "correct horse battery")

        then: "The old password no longer authenticates"
            thrown(InvalidCredentialsException)

        when: "The same token is used again"
            resetPassword.reset("first-token", "another safe password")

        then: "The second attempt is rejected generically"
            thrown(InvalidPasswordResetTokenException)

        when: "A new token is allowed to expire"
            requestPasswordReset.request("owner@example.com", "127.0.0.1")
            clock.plus(java.time.Duration.ofMinutes(31))
            resetPassword.reset("second-token", "another safe password")

        then: "The expired token is rejected generically"
            thrown(InvalidPasswordResetTokenException)
    }

    def "rate limited reset request records no token but remains no-op"() {
        given: "An enabled account exists and the reset limiter blocks requests"
            saveUser("owner@example.com", "OwnerOne", "hashed:correct horse battery")
            rateLimiter.blockPasswordReset()

        when: "The user asks for a reset"
            requestPasswordReset.request("owner@example.com", "127.0.0.1")

        then: "No token or notification is created"
            resetTokens.count() == 0
            notifications.sent().isEmpty()
    }

    private UserAccount saveUser(String email, String username, String passwordHash) {
        userAccounts.save(UserAccount.register(
                new UserId(UUID.randomUUID()),
                new EmailAddress(email),
                new Username(username),
                passwordHash,
                clock.instant()
        ))
    }
}
