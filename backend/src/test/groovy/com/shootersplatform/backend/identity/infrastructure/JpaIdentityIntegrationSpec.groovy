package com.shootersplatform.backend.identity.infrastructure

import com.shootersplatform.backend.AbstractIntegrationSpec
import com.shootersplatform.backend.identity.domain.EmailAddress
import com.shootersplatform.backend.identity.domain.UserAccountRepository
import com.shootersplatform.backend.identity.domain.Username
import com.shootersplatform.backend.identity.usecase.RegisterUserUseCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.simple.JdbcClient

class JpaIdentityIntegrationSpec extends AbstractIntegrationSpec {

  @Autowired
  RegisterUserUseCase registerUser

  @Autowired
  UserAccountRepository userAccounts

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
}
