package com.shootersplatform.backend.identity.infrastructure

import com.shootersplatform.backend.identity.domain.EmailAddress
import com.shootersplatform.backend.identity.domain.UserAccountRepository
import com.shootersplatform.backend.identity.usecase.RegisterUserUseCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.postgresql.PostgreSQLContainer
import spock.lang.Specification

@ActiveProfiles("test")
@SpringBootTest
class JpaIdentityIntegrationSpec extends Specification {

    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.3-alpine")

    static {
        postgres.start()
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl)
        registry.add("spring.datasource.username", postgres::getUsername)
        registry.add("spring.datasource.password", postgres::getPassword)
    }

    def cleanupSpec() {
        postgres.stop()
    }

    @Autowired
    RegisterUserUseCase registerUser

    @Autowired
    UserAccountRepository userAccounts

    @Autowired
    JdbcClient jdbcClient

    def "flyway creates identity schema and persists user with role"() {
        given: "A unique email address for an integration test user"
        def email = "integration-${UUID.randomUUID()}@example.com"

        when: "The user is registered through the application use case"
        def registered = registerUser.register(email, "correct horse battery", "127.0.0.1")

        then: "The user account can be loaded through the repository"
        userAccounts.findByEmail(new EmailAddress(email)).present

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
