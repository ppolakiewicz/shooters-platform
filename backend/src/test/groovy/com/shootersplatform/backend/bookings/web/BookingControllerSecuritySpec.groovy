package com.shootersplatform.backend.bookings.web

import com.jayway.jsonpath.JsonPath
import com.shootersplatform.backend.AbstractIntegrationSpec
import com.shootersplatform.backend.identity.web.AuthApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.time.LocalDateTime

import static org.hamcrest.Matchers.hasSize
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class BookingControllerSecuritySpec extends AbstractIntegrationSpec {

    @Autowired
    WebApplicationContext context

    MockMvc mockMvc
    AuthApiClient auth
    ReservationApiClient reservations
    WaitlistApiClient waitlist
    TermApiClient terms
    TrainingTemplateApiClient trainingTemplates

    @Autowired
    JdbcClient jdbcClient

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build()
        auth = new AuthApiClient(mockMvc)
        reservations = new ReservationApiClient(mockMvc)
        waitlist = new WaitlistApiClient(mockMvc)
        terms = new TermApiClient(mockMvc)
        trainingTemplates = new TrainingTemplateApiClient(mockMvc)
    }

    def "management booking endpoints require authenticated user and csrf"() {
        given: "A random term id is known"
            def termId = UUID.randomUUID()

        expect: "Anonymous management requests are rejected"
            reservations.listWithoutSession(termId).andExpect(status().isUnauthorized())
            waitlist.listWithoutSession(termId).andExpect(status().isUnauthorized())

        and: "Mutating term requests require CSRF"
            terms.createWithoutCsrf(registerSession()).andExpect(status().isForbidden())
    }

    def "reservation create response exposes only cancellation token and management response exposes no secrets"() {
        given: "A user owns a one-seat term"
            def session = registerSession()
            def termId = UUID.fromString(json(terms.create(session, "Secure term", 1, LocalDateTime.parse("2026-06-01T12:00:00"))
                    .andExpect(status().isCreated())
                    .andReturn(), '$.id') as String)

        when: "A participant reserves a place"
            def createResult = reservations.reserve(termId, "Anna", "Nowak", "anna-${UUID.randomUUID()}@example.com")

        then: "The participant receives a cancellation token but no waitlist confirmation token"
            createResult.andExpect(status().isCreated())
                    .andExpect(jsonPath('$.reservation.cancellationToken').isString())
                    .andExpect(jsonPath('$.reservation.waitlistConfirmationToken').doesNotExist())

        and: "The management list never exposes secret tokens"
            reservations.list(session, termId)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$', hasSize(1)))
                    .andExpect(jsonPath('$[0].cancellationToken').doesNotExist())
                    .andExpect(jsonPath('$[0].waitlistConfirmationToken').doesNotExist())
    }

    def "term update preserves capacity after creation"() {
        given: "A user owns a term"
            def session = registerSession()
            def termId = UUID.fromString(json(terms.create(session, "Capacity term", 2, LocalDateTime.parse("2026-06-01T12:00:00")).andReturn(), '$.id') as String)

        when: "The owner sends a different capacity in the update payload"
            def result = terms.update(session, termId, "Capacity term", 1, LocalDateTime.parse("2026-06-01T12:00:00"))

        then: "The update succeeds and keeps the original capacity"
            result.andExpect(status().isOk())
                    .andExpect(jsonPath('$.capacity').value(2))
    }

    def "training template endpoints require organizer role"() {
        given: "A regular user session and a random template id"
            def userSession = registerSession()
            def templateId = UUID.randomUUID()

        expect: "Anonymous requests are unauthorized and regular users are forbidden"
            trainingTemplates.listWithoutSession().andExpect(status().isUnauthorized())
            trainingTemplates.list(userSession).andExpect(status().isForbidden())
            trainingTemplates.get(userSession, templateId).andExpect(status().isForbidden())
            trainingTemplates.create(userSession, "Forbidden").andExpect(status().isForbidden())
            trainingTemplates.update(userSession, templateId, "Forbidden").andExpect(status().isForbidden())
            trainingTemplates.delete(userSession, templateId).andExpect(status().isForbidden())

        and: "An organizer can reach the endpoint"
            trainingTemplates.list(organizerSession()).andExpect(status().isOk())
    }

    def "training template request validation reports each invalid bean value"(
            String name,
            String description,
            int capacity,
            int cancellationDays,
            int durationMinutes
    ) {
        given: "An authenticated organizer"
            def session = organizerSession()

        expect: "The invalid request is rejected before domain execution"
            trainingTemplates.create(
                    session,
                    name,
                    description,
                    com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel.BASIC,
                    capacity,
                    cancellationDays,
                    durationMinutes,
                    "09:15"
            )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath('$.title').value('Invalid request'))

        where:
            name            | description      | capacity | cancellationDays | durationMinutes
            ""              | ""               | 8        | 1                | 90
            "n".repeat(121) | ""               | 8        | 1                | 90
            "Valid"         | "d".repeat(2049) | 8        | 1                | 90
            "Valid"         | ""               | 0        | 1                | 90
            "Valid"         | ""               | 11       | 1                | 90
            "Valid"         | ""               | 8        | -1               | 90
            "Valid"         | ""               | 8        | 366              | 90
            "Valid"         | ""               | 8        | 1                | 29
            "Valid"         | ""               | 8        | 1                | 1441
    }

    def "training template request rejects missing required object values"() {
        given: "An authenticated organizer and a request missing required object fields"
            def session = organizerSession()
            def body = """
                    {
                      "capacity": 8,
                      "cancellationDeadlineDays": 1,
                      "durationMinutes": 90
                    }
                    """

        expect: "Bean validation returns problem JSON"
            trainingTemplates.createRaw(session, body)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath('$.title').value('Invalid request'))
    }

    def "training template domain validation reports unsupported time steps"() {
        given: "An authenticated organizer"
            def session = organizerSession()

        expect: "A non-quarter-hour start is rejected by domain validation"
            trainingTemplates.create(
                    session,
                    "Invalid time",
                    com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel.BASIC,
                    8,
                    90,
                    "09:01"
            )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath('$.title').value('Invalid booking request'))
    }

    private MockHttpSession registerSession() {
        def result = auth.register(uniqueEmail(), uniqueUsername(), "correct horse battery").andExpect(status().isCreated()).andReturn()
        result.request.getSession(false) as MockHttpSession
    }

    private MockHttpSession organizerSession() {
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
        def result = auth.login(email, "correct horse battery").andExpect(status().isOk()).andReturn()
        result.request.getSession(false) as MockHttpSession
    }

    private static String uniqueEmail() {
        "booking-${UUID.randomUUID()}@example.com"
    }

    private static String uniqueUsername() {
        "Booking_${UUID.randomUUID().toString().replace("-", "").substring(0, 12)}"
    }

    private static Object json(result, String path) {
        JsonPath.parse(result.response.contentAsString).read(path)
    }
}
