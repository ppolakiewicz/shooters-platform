package com.shootersplatform.backend

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

@ActiveProfiles("test")
@SpringBootTest
class ShootersPlatformApplicationSpec extends Specification {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.3-alpine")

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

    def "loads application context"() {
        expect: "The Spring application context starts successfully"
        true
    }
}
