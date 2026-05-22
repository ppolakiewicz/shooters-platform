package com.shootersplatform.backend.bookings.web

import com.jayway.jsonpath.JsonPath
import com.shootersplatform.backend.AbstractIntegrationSpec
import com.shootersplatform.backend.identity.web.AuthApiClient
import org.springframework.beans.factory.annotation.Autowired
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

  def setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build()
    auth = new AuthApiClient(mockMvc)
    reservations = new ReservationApiClient(mockMvc)
    waitlist = new WaitlistApiClient(mockMvc)
    terms = new TermApiClient(mockMvc)
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

  private MockHttpSession registerSession() {
    def result = auth.register(uniqueEmail(), uniqueUsername(), "correct horse battery").andExpect(status().isCreated()).andReturn()
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
