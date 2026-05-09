package com.shootersplatform.backend.bookings.web

import com.jayway.jsonpath.JsonPath
import com.shootersplatform.backend.AbstractIntegrationSpec
import com.shootersplatform.backend.bookings.BookingApiClient
import com.shootersplatform.backend.identity.web.AuthApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.time.LocalDateTime

import static org.hamcrest.Matchers.hasSize
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity

class BookingControllerSecuritySpec extends AbstractIntegrationSpec {

  @Autowired
  WebApplicationContext context

  MockMvc mockMvc
  AuthApiClient auth
  BookingApiClient bookings

  def setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build()
    auth = new AuthApiClient(mockMvc)
    bookings = new BookingApiClient(mockMvc)
  }

  def "management booking endpoints require authenticated user and csrf"() {
    given: "A random term id is known"
        def termId = UUID.randomUUID()

    expect: "Anonymous management requests are rejected"
        bookings.listReservationsWithoutSession(termId).andExpect(status().isUnauthorized())

    and: "Mutating term requests require CSRF"
        bookings.createTermWithoutCsrf(registerSession()).andExpect(status().isForbidden())
  }

  def "reservation create response exposes only cancellation token and management response exposes no secrets"() {
    given: "A user owns a one-seat term"
        def session = registerSession()
        def termId = UUID.fromString(json(bookings.createTerm(session, "Secure term", 1, LocalDateTime.parse("2026-06-01T12:00:00"))
        .andExpect(status().isCreated())
        .andReturn(), '$.id') as String)

    when: "A participant reserves a place"
        def createResult = bookings.reserve(termId, "Anna", "Nowak", "anna-${UUID.randomUUID()}@example.com")

    then: "The participant receives a cancellation token but no waitlist confirmation token"
        createResult.andExpect(status().isCreated())
        .andExpect(jsonPath('$.cancellationToken').isString())
        .andExpect(jsonPath('$.waitlistConfirmationToken').doesNotExist())

    and: "The management list never exposes secret tokens"
        bookings.listReservations(session, termId)
        .andExpect(status().isOk())
        .andExpect(jsonPath('$', hasSize(1)))
        .andExpect(jsonPath('$[0].cancellationToken').doesNotExist())
        .andExpect(jsonPath('$[0].waitlistConfirmationToken').doesNotExist())
  }

  def "term update rejects capacity below occupied places"() {
    given: "A user owns a term with two occupied places"
        def session = registerSession()
        def termId = UUID.fromString(json(bookings.createTerm(session, "Capacity term", 2, LocalDateTime.parse("2026-06-01T12:00:00")).andReturn(), '$.id') as String)
        bookings.reserve(termId, "Anna", "Nowak", "anna-${UUID.randomUUID()}@example.com").andExpect(status().isCreated())
        bookings.reserve(termId, "Jan", "Kowalski", "jan-${UUID.randomUUID()}@example.com").andExpect(status().isCreated())

    when: "The owner lowers capacity below occupied places"
        def result = bookings.updateTerm(session, termId, "Capacity term", 1, LocalDateTime.parse("2026-06-01T12:00:00"))

    then: "The API rejects the update"
        result.andExpect(status().isBadRequest())
        .andExpect(jsonPath('$.title').value('Invalid term request'))
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
