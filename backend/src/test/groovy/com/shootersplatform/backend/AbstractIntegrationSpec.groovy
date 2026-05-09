package com.shootersplatform.backend

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.postgresql.PostgreSQLContainer
import spock.lang.Specification

@ActiveProfiles("test")
@SpringBootTest
abstract class AbstractIntegrationSpec extends Specification {

  private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18.3-alpine")

  static {
    POSTGRES.start()
  }

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl)
    registry.add("spring.datasource.username", POSTGRES::getUsername)
    registry.add("spring.datasource.password", POSTGRES::getPassword)
  }
}
