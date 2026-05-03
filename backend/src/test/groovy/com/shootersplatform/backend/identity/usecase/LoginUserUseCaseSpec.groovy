package com.shootersplatform.backend.identity.usecase

import com.shootersplatform.backend.identity.InMemoryLoginRateLimiter
import com.shootersplatform.backend.identity.InMemoryUserAccountRepository
import com.shootersplatform.backend.identity.PlainTextPasswordHasher
import com.shootersplatform.backend.identity.domain.EmailAddress
import com.shootersplatform.backend.identity.domain.IdentityService
import com.shootersplatform.backend.identity.domain.InvalidCredentialsException
import com.shootersplatform.backend.identity.domain.RateLimitExceededException
import com.shootersplatform.backend.identity.domain.UserAccount
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class LoginUserUseCaseSpec extends Specification {

    private InMemoryUserAccountRepository userAccounts
    private InMemoryLoginRateLimiter rateLimiter
    private LoginUserUseCase loginUser

    def setup() {
        userAccounts = new InMemoryUserAccountRepository()
        rateLimiter = new InMemoryLoginRateLimiter()
        def identity = new IdentityService(
                userAccounts,
                new PlainTextPasswordHasher(),
                Clock.fixed(Instant.parse("2026-05-03T12:00:00Z"), ZoneOffset.UTC)
        )
        loginUser = new LoginUserUseCase(
                identity,
                rateLimiter
        )
    }

    def "logs in registered user"() {
        given: "An enabled user account exists in the identity domain"
        userAccounts.save(UserAccount.register(
                UUID.randomUUID(),
                new EmailAddress("owner@example.com"),
                "hashed:correct horse battery",
                Instant.now()
        ))

        when: "The user logs in with matching credentials and different email casing"
        def user = loginUser.login("OWNER@example.com", "correct horse battery", "127.0.0.1")

        then: "The authenticated user is returned with normalized email and USER role"
        user.email() == "owner@example.com"
        user.roles() == ["USER"] as Set
    }

    def "rejects invalid password and records failure"() {
        given: "An enabled user account exists in the identity domain"
        userAccounts.save(UserAccount.register(
                UUID.randomUUID(),
                new EmailAddress("owner@example.com"),
                "hashed:correct horse battery",
                Instant.now()
        ))

        when: "The user logs in with an invalid password"
        loginUser.login("owner@example.com", "wrong password value", "127.0.0.1")

        then: "The login is rejected with a generic credential error"
        thrown(InvalidCredentialsException)

        and: "The rate limiter records the failed login"
        rateLimiter.loginFailures() == 1
    }

    def "rejects login when rate limited"() {
        given: "The rate limiter blocks login attempts"
        rateLimiter.blockLogin()

        when: "A login attempt is made"
        loginUser.login("owner@example.com", "correct horse battery", "127.0.0.1")

        then: "The login is rejected because too many attempts were made"
        thrown(RateLimitExceededException)
    }
}
