package com.shootersplatform.backend.bookings.web

import com.jayway.jsonpath.JsonPath
import com.shootersplatform.backend.AbstractIntegrationSpec
import com.shootersplatform.backend.identity.web.AuthApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.util.time.MutableClock

import java.time.Instant
import java.time.LocalDateTime

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class TermUserPathIntegrationSpec extends AbstractIntegrationSpec {

    private static final Instant BASE_TIME = Instant.parse("2026-05-08T10:00:00Z")
    private static final LocalDateTime FUTURE_START = LocalDateTime.parse("2026-06-01T12:00:00")
    private static final String PASSWORD = "correct horse battery"

    @Autowired
    WebApplicationContext context

    @Autowired
    MutableClock clock

    MockMvc mockMvc
    AuthApiClient auth
    TermApiClient terms
    ReservationApiClient reservations

    def setup() {
        clock.setInstant(BASE_TIME)
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build()
        auth = new AuthApiClient(mockMvc)
        terms = new TermApiClient(mockMvc)
        reservations = new ReservationApiClient(mockMvc)
    }

    def "creates term and exposes it to owner and public users"() {
        given: "An instructor is signed in"
            def session = registerSession()
            def name = uniqueLabel("Intro pistol")

        when: "The instructor creates a future term"
            def created = terms.create(session, name, 8, FUTURE_START)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath('$.name').value(name))
                    .andExpect(jsonPath('$.capacity').value(8))
                    .andReturn()
            def termId = UUID.fromString(json(created, '$.id') as String)

        then: "The owner sees the term in management listing"
            def ownerTerms = terms.list(session).andExpect(status().isOk()).andReturn()
            termById(ownerTerms, termId).name == name

        and: "Public users see the same term details"
            def publicTerms = terms.publicTerms().andExpect(status().isOk()).andReturn()
            termById(publicTerms, termId).name == name
            terms.publicTerm(termId)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.name').value(name))
                    .andExpect(jsonPath('$.location.placeName').value('Range A'))
    }

    def "updates term and public view reflects edited snapshot"() {
        given: "An instructor owns a term"
            def session = registerSession()
            def termId = createTerm(session, uniqueLabel("Editable term"), 4, FUTURE_START)
            def updatedBody = TermApiClient.termBody(
                    "Updated pistol",
                    "Advanced safety",
                    "Range B",
                    "Second Street 2",
                    50.0614d,
                    19.9366d,
                    6,
                    2,
                    90,
                    LocalDateTime.parse("2026-06-02T18:30:00")
            )

        when: "The instructor edits the term"
            terms.update(session, termId, updatedBody).andExpect(status().isOk())

        then: "The public term view exposes the updated snapshot"
            terms.publicTerm(termId)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.name').value('Updated pistol'))
                    .andExpect(jsonPath('$.description').value('Advanced safety'))
                    .andExpect(jsonPath('$.location.placeName').value('Range B'))
                    .andExpect(jsonPath('$.capacity').value(4))
                    .andExpect(jsonPath('$.durationMinutes').value(90))
                    .andExpect(jsonPath('$.startsAt').value('2026-06-02T18:30:00'))
    }

    def "rejects invalid term requests"() {
        given: "An instructor is signed in"
            def session = registerSession()

        expect: "Invalid term request is rejected"
            terms.create(session, body).andExpect(status().isBadRequest())

        where:
            body << [
                    TermApiClient.termBody("", 1, FUTURE_START),
                    TermApiClient.termBody("No capacity", 0, FUTURE_START),
                    TermApiClient.termBody("Bad latitude", "", "Range A", "Range Street 1", 91.0d, 21.0122d, 1, 1, 60, FUTURE_START),
                    """
          {
            "name": "Missing start",
            "description": "",
            "location": {
              "placeName": "Range A",
              "address": "Range Street 1",
              "latitude": 52.2297,
              "longitude": 21.0122
            },
            "capacity": 1,
            "cancellationDeadlineDays": 1,
            "durationMinutes": 60
          }
          """
            ]
    }

    def "hides past terms from public listing"() {
        given: "Past and future terms exist"
            def session = registerSession()
            def pastId = createTerm(session, uniqueLabel("Past term"), 1, LocalDateTime.parse("2026-05-08T11:59:59"))
            def futureId = createTerm(session, uniqueLabel("Future term"), 1, LocalDateTime.parse("2026-05-08T12:00:01"))

        when: "Public users list available terms"
            def result = terms.publicTerms().andExpect(status().isOk()).andReturn()

        then: "Only the future term is returned"
            termByIdOrNull(result, pastId) == null
            termById(result, futureId) != null
    }

    def "public term view reports available places after confirmed reservations"() {
        given: "An instructor owns a term with three places"
            def session = registerSession()
            def termId = createTerm(session, uniqueLabel("Available term"), 3, FUTURE_START)

        when: "A participant reserves one place"
            reservations.reserve(termId, "Anna", "Nowak", uniqueEmail()).andExpect(status().isCreated())

        then: "Public users see remaining places instead of only maximum capacity"
            def publicTerms = terms.publicTerms().andExpect(status().isOk()).andReturn()
            termById(publicTerms, termId).capacity == 3
            termById(publicTerms, termId).availablePlaces == 2

        and: "The public term details expose the same availability"
            terms.publicTerm(termId)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath('$.capacity').value(3))
                    .andExpect(jsonPath('$.availablePlaces').value(2))
    }

    def "non-owner cannot update another instructor term"() {
        given: "A term owned by another instructor exists"
            def ownerSession = registerSession()
            def otherSession = registerSession()
            def termId = createTerm(ownerSession, uniqueLabel("Private term"), 1, FUTURE_START)

        expect: "The other instructor cannot update the owner term"
            terms.update(otherSession, termId, "Hacked", 1, FUTURE_START).andExpect(status().isNotFound())
    }

    private MockHttpSession registerSession() {
        def result = auth.register(uniqueEmail(), uniqueUsername(), PASSWORD).andExpect(status().isCreated()).andReturn()
        result.request.getSession(false) as MockHttpSession
    }

    private UUID createTerm(MockHttpSession session, String name, int capacity, LocalDateTime startsAt) {
        def result = terms.create(session, name, capacity, startsAt).andExpect(status().isCreated()).andReturn()
        UUID.fromString(json(result, '$.id') as String)
    }

    private static Map<String, Object> termById(MvcResult result, UUID termId) {
        def term = termByIdOrNull(result, termId)
        assert term != null
        term
    }

    private static Map<String, Object> termByIdOrNull(MvcResult result, UUID termId) {
        (json(result, '$') as List<Map<String, Object>>).find { term -> term.id == termId.toString() }
    }

    private static Object json(MvcResult result, String path) {
        JsonPath.parse(result.response.contentAsString).read(path)
    }

    private static String uniqueEmail() {
        "booking-${UUID.randomUUID()}@example.com"
    }

    private static String uniqueUsername() {
        "Booking_${UUID.randomUUID().toString().replace("-", "").substring(0, 12)}"
    }

    private static String uniqueLabel(String prefix) {
        "${prefix} ${UUID.randomUUID().toString().substring(0, 8)}"
    }
}
