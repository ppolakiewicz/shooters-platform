package com.shootersplatform.backend.bookings.web

import com.jayway.jsonpath.JsonPath
import com.shootersplatform.backend.AbstractIntegrationSpec
import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel
import com.shootersplatform.backend.identity.web.AuthApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.util.time.MutableClock

import java.time.Duration
import java.time.Instant

import static org.hamcrest.Matchers.hasSize
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class TrainingTemplateUserPathIntegrationSpec extends AbstractIntegrationSpec {

    private static final Instant BASE_TIME = Instant.parse("2026-05-08T10:00:00Z")
    private static final String PASSWORD = "correct horse battery"

    @Autowired
    WebApplicationContext context

    @Autowired
    JdbcClient jdbcClient

    @Autowired
    MutableClock clock

    MockMvc mockMvc
    AuthApiClient auth
    TrainingTemplateApiClient trainingTemplates

    def setup() {
        clock.setInstant(BASE_TIME)
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build()
        auth = new AuthApiClient(mockMvc)
        trainingTemplates = new TrainingTemplateApiClient(mockMvc)
    }

    def "organizer manages private training templates through their full lifecycle"() {
        given: "Two organizers"
            def ownerSession = organizerSession()
            def otherSession = organizerSession()

        when: "The owner creates two templates and another organizer creates a private template"
            def first = trainingTemplates.create(ownerSession, "Basic pistol")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath('$.name').value('Basic pistol'))
                    .andReturn()
            def firstId = UUID.fromString(json(first, '$.id') as String)
            clock.plus(Duration.ofMinutes(1))
            def second = trainingTemplates.create(
                    ownerSession,
                    "Advanced pistol",
                    "Advanced training",
                    TrainingLevel.ADVANCED,
                    10,
                    365,
                    1440,
                    "23:45"
            )
                    .andExpect(status().isCreated())
                    .andReturn()
            def secondId = UUID.fromString(json(second, '$.id') as String)
            trainingTemplates.create(otherSession, "Other organizer template")
                    .andExpect(status().isCreated())

        then: "The owner sees only their templates ordered by the latest update"
            def ownerList = trainingTemplates.list(ownerSession)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$', hasSize(2)))
                    .andReturn()
            ids(ownerList) == [secondId, firstId]

        and: "Template details expose the saved business data"
            trainingTemplates.get(ownerSession, secondId)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.name').value('Advanced pistol'))
                    .andExpect(jsonPath('$.description').value('Advanced training'))
                    .andExpect(jsonPath('$.trainingLevel').value('ADVANCED'))
                    .andExpect(jsonPath('$.capacity').value(10))
                    .andExpect(jsonPath('$.cancellationDeadlineDays').value(365))
                    .andExpect(jsonPath('$.durationMinutes').value(1440))
                    .andExpect(jsonPath('$.defaultStartTime').value('23:45'))

        when: "The owner updates the older template"
            clock.plus(Duration.ofMinutes(1))
            trainingTemplates.update(
                    ownerSession,
                    firstId,
                    "Updated pistol",
                    "Updated description",
                    TrainingLevel.INTERMEDIATE,
                    7,
                    7,
                    120,
                    "10:30"
            )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.name').value('Updated pistol'))
                    .andExpect(jsonPath('$.trainingLevel').value('INTERMEDIATE'))

        then: "The edited template moves to the front"
            ids(trainingTemplates.list(ownerSession).andExpect(status().isOk()).andReturn()) == [firstId, secondId]

        when: "The owner deletes the edited template"
            trainingTemplates.delete(ownerSession, firstId)
                    .andExpect(status().isNoContent())

        then: "The deleted template disappears and its details return not found"
            ids(trainingTemplates.list(ownerSession).andExpect(status().isOk()).andReturn()) == [secondId]
            trainingTemplates.get(ownerSession, firstId)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath('$.title').value('Booking resource not found'))

        and: "Another organizer cannot read the remaining private template"
            trainingTemplates.get(otherSession, secondId)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath('$.title').value('Booking resource not found'))
    }

    private MockHttpSession organizerSession() {
        def email = uniqueEmail()
        auth.register(email, uniqueUsername(), PASSWORD).andExpect(status().isCreated())
        jdbcClient.sql("""
                insert into user_account_roles (user_account_id, role_name)
                select id, 'ORGANIZER'
                from user_accounts
                where email = :email
                on conflict do nothing
                """)
                .param("email", email)
                .update()
        def result = auth.login(email, PASSWORD).andExpect(status().isOk()).andReturn()
        result.request.getSession(false) as MockHttpSession
    }

    private static List<UUID> ids(MvcResult result) {
        (json(result, '$[*].id') as List<String>).collect(UUID::fromString)
    }

    private static Object json(MvcResult result, String path) {
        JsonPath.parse(result.response.contentAsString).read(path)
    }

    private static String uniqueEmail() {
        "template-${UUID.randomUUID()}@example.com"
    }

    private static String uniqueUsername() {
        "Template_${UUID.randomUUID().toString().replace("-", "").substring(0, 12)}"
    }
}
