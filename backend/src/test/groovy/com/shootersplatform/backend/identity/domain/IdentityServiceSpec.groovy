package com.shootersplatform.backend.identity.domain

import com.shootersplatform.backend.identity.InMemoryUserAccountRepository
import com.shootersplatform.backend.identity.PlainTextPasswordHasher
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class IdentityServiceSpec extends Specification {

    private InMemoryUserAccountRepository userAccounts
    private IdentityService identity

    def setup() {
        userAccounts = new InMemoryUserAccountRepository()
        identity = new IdentityService(
                userAccounts,
                new PlainTextPasswordHasher(),
                Clock.fixed(Instant.parse("2026-05-03T12:00:00Z"), ZoneOffset.UTC)
        )
    }

    def "registers active user with default role normalized email and display username"() {
        when: "The identity domain registers a user with mixed-case email, display username, and a valid password"
            def account = identity.register("  NEW.User@Example.COM  ", "  Shooter_One  ", "correct horse battery")

        then: "The account has normalized email, display username, active status, and the default USER role"
            account.email().value() == "new.user@example.com"
            account.username().value() == "Shooter_One"
            account.username().normalized() == "shooter_one"
            account.enabled()
            account.roles() == [UserRole.USER] as Set
            userAccounts.count() == 1

        and: "The account stores a hashed password through the password hasher port"
            account.passwordHash() == "hashed:correct horse battery"
    }

    def "rejects duplicate email during registration"() {
        given: "An account already exists for the normalized email"
            identity.register("owner@example.com", "OwnerOne", "correct horse battery")

        when: "The identity domain registers the same email with different casing and whitespace"
            identity.register(" OWNER@example.com ", "OtherOne", "another safe password")

        then: "The domain rejects the duplicate email"
            thrown(DuplicateEmailException)
    }

    def "rejects duplicate username during registration case insensitively"() {
        given: "An account already exists for the normalized username"
            identity.register("owner@example.com", "Shooter_One", "correct horse battery")

        when: "The identity domain registers the same username with different casing"
            identity.register("other@example.com", "shooter_one", "another safe password")

        then: "The domain rejects the duplicate username"
            thrown(DuplicateUsernameException)
    }

    def "rejects invalid username during registration"() {
        when: "The identity domain registers a username with unsupported characters"
            identity.register("owner@example.com", "bad.name", "correct horse battery")

        then: "The domain username policy rejects the request"
            def exception = thrown(IdentityValidationException)
            exception.message.contains("letters, numbers, underscores, and hyphens")
    }

    def "rejects weak password during registration"() {
        when: "The identity domain registers a password shorter than the policy minimum"
            identity.register("owner@example.com", "OwnerOne", "short")

        then: "The domain password policy rejects the request"
            def exception = thrown(IdentityValidationException)
            exception.message.contains("at least 12")
    }

    def "authenticates enabled account with matching password"() {
        given: "A registered account exists"
            identity.register("owner@example.com", "OwnerOne", "correct horse battery")

        when: "The identity domain authenticates with matching credentials"
            def account = identity.authenticate(new EmailAddress("OWNER@example.com"), "correct horse battery")

        then: "The registered account is returned"
            account.email().value() == "owner@example.com"
            account.username().value() == "OwnerOne"
            account.roles() == [UserRole.USER] as Set
    }

    def "rejects authentication with invalid password"() {
        given: "A registered account exists"
            identity.register("owner@example.com", "OwnerOne", "correct horse battery")

        when: "The identity domain authenticates with an invalid password"
            identity.authenticate(new EmailAddress("owner@example.com"), "wrong password value")

        then: "The domain rejects the credentials generically"
            thrown(InvalidCredentialsException)
    }
}
