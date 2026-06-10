package com.shootersplatform.backend.identity.web

import com.shootersplatform.backend.AbstractIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.hamcrest.Matchers.*
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AuthControllerSecuritySpec extends AbstractIntegrationSpec {

    @Autowired
    WebApplicationContext context

    AuthApiClient auth

    @Autowired
    JdbcClient jdbcClient

    def setup() {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build()
        auth = new AuthApiClient(mockMvc)
    }

    def "public actuator health endpoint stays accessible"() {
        when: "The public actuator health endpoint is requested"
            def result = auth.health()

        then: "The backend responds successfully without authentication"
            result.andExpect(status().isOk())
    }

    def "register creates session and current user"() {
        when: "A new user registers with valid credentials"
            def username = uniqueUsername()
            def result = auth.register(uniqueEmail(), username, "correct horse battery")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath('$.email').value(endsWith('@example.com')))
                    .andExpect(jsonPath('$.username').value(username))
                    .andExpect(jsonPath('$.roles', contains('USER')))
                    .andReturn()

        then: "The created session can be used to load the current user"
            def session = result.request.getSession(false) as MockHttpSession
            auth.me(session)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.username').value(username))
                    .andExpect(jsonPath('$.roles', contains('USER')))
    }

    def "login and current user expose every persisted role"() {
        given: "A registered user with an administratively assigned organizer role"
            def email = uniqueEmail()
            auth.register(email, uniqueUsername(), "correct horse battery").andExpect(status().isCreated())
            jdbcClient.sql("""
                    insert into user_account_roles (user_account_id, role_name)
                    select id, 'ORGANIZER'
                    from user_accounts
                    where email = :email
                    on conflict do nothing
                    """)
                    .param("email", email)
                    .update()

        when: "The user logs in"
            def result = auth.login(email, "correct horse battery")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.roles', containsInAnyOrder('USER', 'ORGANIZER')))
                    .andReturn()

        then: "Login and current-user responses expose both roles"
            auth.me(result.request.getSession(false) as MockHttpSession)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.roles', containsInAnyOrder('USER', 'ORGANIZER')))
    }

    def "register rejects duplicate email"() {
        given: "A user has already registered an email address"
            def email = uniqueEmail()
            auth.register(email, uniqueUsername(), "correct horse battery").andExpect(status().isCreated())

        when: "Another registration uses the same email address with different casing"
            def result = auth.register(email.toUpperCase(), uniqueUsername(), "another safe password")

        then: "The API reports a conflict"
            result.andExpect(status().isConflict())
                    .andExpect(jsonPath('$.title').value('Email is already registered'))
    }

    def "register rejects duplicate username case insensitively"() {
        given: "A user has already registered a username"
            def username = uniqueUsername()
            auth.register(uniqueEmail(), username, "correct horse battery").andExpect(status().isCreated())

        when: "Another registration uses the same username with different casing"
            def result = auth.register(uniqueEmail(), username.toLowerCase(), "another safe password")

        then: "The API reports a conflict"
            result.andExpect(status().isConflict())
                    .andExpect(jsonPath('$.title').value('Username is already registered'))
    }

    def "register requires username"() {
        when: "Registration is submitted without a username"
            def result = auth.register(uniqueEmail(), "", "correct horse battery")

        then: "The API rejects the request"
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath('$.title').value('Invalid request'))
    }

    def "protected current user endpoint requires authentication"() {
        when: "The current user endpoint is requested without an authenticated session"
            def result = auth.me(new MockHttpSession())

        then: "The API rejects the request as unauthorized"
            result.andExpect(status().isUnauthorized())
    }

    def "csrf is required for registration"() {
        when: "Registration is submitted without a CSRF token"
            def result = auth.registerWithoutCsrf(uniqueEmail(), uniqueUsername(), "correct horse battery")

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

    def "password reset request is public and always no content"() {
        given: "A user has registered"
            def email = uniqueEmail()
            auth.register(email, uniqueUsername(), "correct horse battery").andExpect(status().isCreated())

        expect: "Both known and unknown addresses receive the same empty response"
            auth.requestPasswordReset(email).andExpect(status().isNoContent())
            auth.requestPasswordReset(uniqueEmail()).andExpect(status().isNoContent())
    }

    def "password reset request requires csrf"() {
        when: "Password reset is requested without a CSRF token"
            def result = auth.requestPasswordResetWithoutCsrf(uniqueEmail())

        then: "Spring Security rejects the request"
            result.andExpect(status().isForbidden())
    }

    def "password reset rejects invalid token generically"() {
        when: "Password reset is submitted with an invalid token"
            def result = auth.resetPassword("not-a-real-token", "new correct password")

        then: "The API returns a generic token error"
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath('$.title').value('Invalid password reset link'))
                    .andExpect(jsonPath('$.detail').value('Password reset link is invalid or expired'))
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
            def registerResult = auth.register(uniqueEmail(), uniqueUsername(), "correct horse battery").andReturn()
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

    private static String uniqueUsername() {
        "User_${UUID.randomUUID().toString().replace("-", "").substring(0, 12)}"
    }
}
