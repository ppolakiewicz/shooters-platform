package com.shootersplatform.backend.identity.infrastructure

import com.shootersplatform.backend.AbstractIntegrationSpec
import com.shootersplatform.backend.identity.domain.*
import com.shootersplatform.backend.identity.usecase.LoginUserUseCase
import com.shootersplatform.backend.identity.usecase.RegisterUserUseCase
import com.shootersplatform.backend.identity.usecase.ResetPasswordUseCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.simple.JdbcClient

import java.time.Instant

class JpaIdentityIntegrationSpec extends AbstractIntegrationSpec {

    @Autowired
    RegisterUserUseCase registerUser

    @Autowired
    LoginUserUseCase loginUser

    @Autowired
    ResetPasswordUseCase resetPassword

    @Autowired
    UserAccountRepository userAccounts

    @Autowired
    PasswordResetTokenRepository resetTokens

    @Autowired
    PasswordResetTokenGenerator tokenGenerator

    @Autowired
    JdbcClient jdbcClient

    def "flyway creates identity schema and persists user with role"() {
        given: "A unique email address for an integration test user"
            def email = "integration-${UUID.randomUUID()}@example.com"
            def username = "Integration_${UUID.randomUUID().toString().replace("-", "").substring(0, 12)}"

        when: "The user is registered through the application use case"
            def registered = registerUser.register(email, username, "correct horse battery", "127.0.0.1")

        then: "The user account can be loaded through the repository"
            def persisted = userAccounts.findByEmail(new EmailAddress(email)).orElseThrow()
            persisted.username().value() == username
            userAccounts.existsByUsername(new Username(username.toLowerCase()))

        and: "The USER role is persisted in the join table"
            def roleCount = jdbcClient.sql("""
          select count(*)
          from user_account_roles
          where user_account_id = :userId and role_name = 'USER'
          """)
                    .param("userId", registered.id().value())
                    .query(Integer)
                    .single()

            roleCount == 1
    }

    def "flyway creates password reset token schema and reset updates user password"() {
        given: "A persisted user and password reset token"
            def email = "reset-${UUID.randomUUID()}@example.com"
            def username = "Reset_${UUID.randomUUID().toString().replace("-", "").substring(0, 12)}"
            def registered = registerUser.register(email, username, "correct horse battery", "127.0.0.1")
            def rawToken = "integration-reset-token-${UUID.randomUUID()}"
            resetTokens.save(PasswordResetToken.create(
                    registered.id(),
                    tokenGenerator.hash(rawToken),
                    Instant.parse("2099-01-01T00:30:00Z"),
                    Instant.parse("2099-01-01T00:00:00Z")
            ))

        when: "The reset token is used to change the password"
            resetPassword.reset(rawToken, "new correct password")

        then: "The new password authenticates through the normal login use case"
            loginUser.login(email, "new correct password", "127.0.0.2").username().value() == username

        and: "The token row is marked used"
            jdbcClient.sql("select count(*) from password_reset_tokens where used_at is not null")
                    .query(Integer)
                    .single() >= 1
    }
}
