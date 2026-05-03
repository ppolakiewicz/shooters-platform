package com.shootersplatform.backend.identity.usecase

import com.shootersplatform.backend.identity.InMemoryLoginRateLimiter
import com.shootersplatform.backend.identity.InMemoryUserAccountRepository
import com.shootersplatform.backend.identity.PlainTextPasswordHasher
import com.shootersplatform.backend.identity.domain.IdentityService
import com.shootersplatform.backend.identity.domain.UserRole
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class RegisterUserUseCaseSpec extends Specification {

    private InMemoryUserAccountRepository userAccounts
    private InMemoryLoginRateLimiter rateLimiter
    private RegisterUserUseCase registerUser

    def setup() {
        userAccounts = new InMemoryUserAccountRepository()
        rateLimiter = new InMemoryLoginRateLimiter()
        def identity = new IdentityService(
                userAccounts,
                new PlainTextPasswordHasher(),
                Clock.fixed(Instant.parse("2026-05-03T12:00:00Z"), ZoneOffset.UTC)
        )
        registerUser = new RegisterUserUseCase(
                identity,
                rateLimiter
        )
    }

    def "registers active user with default role and normalized email"() {
        when: "A new user registers with mixed-case email and a valid password"
        def registered = registerUser.register("  NEW.User@Example.COM  ", "correct horse battery", "127.0.0.1")

        then: "The returned user contains the normalized email and default role"
        registered.email() == "new.user@example.com"
        registered.roles() == ["USER"] as Set
        userAccounts.count() == 1

        and: "The persisted account is active, has USER role, and stores the hashed password"
        def saved = userAccounts.savedByEmail("new.user@example.com")
        saved.enabled()
        saved.roles() == [UserRole.USER] as Set
        saved.passwordHash() == "hashed:correct horse battery"
    }

    def "records registration attempt"() {
        when: "A valid registration request is processed"
        registerUser.register("owner@example.com", "correct horse battery", "127.0.0.1")

        then: "The registration attempt is recorded by the rate limiter"
        rateLimiter.registrationAttempts() == 1
    }
}
