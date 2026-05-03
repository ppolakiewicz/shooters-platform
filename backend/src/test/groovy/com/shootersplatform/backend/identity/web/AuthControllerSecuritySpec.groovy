package com.shootersplatform.backend.identity.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.endsWith
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ActiveProfiles("test")
@SpringBootTest
class AuthControllerSecuritySpec extends Specification {

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

    @Autowired
    WebApplicationContext context

    AuthApiClient auth

    def setup() {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build()
        auth = new AuthApiClient(mockMvc)
    }

    def "public health endpoint stays accessible"() {
        when: "The public health endpoint is requested"
        def result = auth.health()

        then: "The backend responds successfully without authentication"
        result.andExpect(status().isOk())
    }

    def "register creates session and current user"() {
        when: "A new user registers with valid credentials"
        def result = auth.register(uniqueEmail(), "correct horse battery")
                .andExpect(status().isCreated())
                .andExpect(jsonPath('$.email').value(endsWith('@example.com')))
                .andExpect(jsonPath('$.roles', contains('USER')))
                .andReturn()

        then: "The created session can be used to load the current user"
        def session = result.request.getSession(false) as MockHttpSession
        auth.me(session)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.roles', contains('USER')))
    }

    def "register rejects duplicate email"() {
        given: "A user has already registered an email address"
        def email = uniqueEmail()
        auth.register(email, "correct horse battery").andExpect(status().isCreated())

        when: "Another registration uses the same email address with different casing"
        def result = auth.register(email.toUpperCase(), "another safe password")

        then: "The API reports a conflict"
        result.andExpect(status().isConflict())
                .andExpect(jsonPath('$.title').value('Email is already registered'))
    }

    def "protected current user endpoint requires authentication"() {
        when: "The current user endpoint is requested without an authenticated session"
        def result = auth.me(new MockHttpSession())

        then: "The API rejects the request as unauthorized"
        result.andExpect(status().isUnauthorized())
    }

    def "csrf is required for registration"() {
        when: "Registration is submitted without a CSRF token"
        def result = auth.registerWithoutCsrf(uniqueEmail(), "correct horse battery")

        then: "Spring Security rejects the request"
        result.andExpect(status().isForbidden())
    }

    def "login failure is generic"() {
        when: "Login is attempted for invalid credentials"
        def result = auth.login(uniqueEmail(), "wrong password value")

        then: "The API returns a generic invalid credentials problem"
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath('$.title').value('Invalid credentials'))
    }

    def "rate limits repeated failed logins"() {
        given: "A client repeatedly fails login for the same email"
        def email = uniqueEmail()
        def clientIp = "203.0.113.9"

        5.times {
            auth.login(email, "wrong password value", clientIp).andExpect(status().isUnauthorized())
        }

        when: "The client makes another failed login attempt in the same window"
        def result = auth.login(email, "wrong password value", clientIp)

        then: "The API rejects the request with a rate limit response"
        result.andExpect(status().isTooManyRequests())
                .andExpect(jsonPath('$.title').value('Too many attempts'))
    }

    def "logout invalidates session"() {
        given: "A registered user has an authenticated session"
        def registerResult = auth.register(uniqueEmail(), "correct horse battery").andReturn()
        def session = registerResult.request.getSession(false) as MockHttpSession

        when: "The user logs out"
        def logoutResult = auth.logout(session)

        then: "Logout succeeds without content"
        logoutResult.andExpect(status().isNoContent())

        and: "The previous session no longer authenticates current user requests"
        auth.me(session).andExpect(status().isUnauthorized())
    }

    private static String uniqueEmail() {
        "user-${UUID.randomUUID()}@example.com"
    }
}
