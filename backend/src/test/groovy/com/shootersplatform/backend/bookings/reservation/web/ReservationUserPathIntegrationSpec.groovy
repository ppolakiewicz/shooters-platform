package com.shootersplatform.backend.bookings.reservation.web

import com.jayway.jsonpath.JsonPath
import com.shootersplatform.backend.AbstractIntegrationSpec
import com.shootersplatform.backend.bookings.term.web.TermApiClient
import com.shootersplatform.backend.bookings.waitlist.web.WaitlistApiClient
import com.shootersplatform.backend.identity.web.AuthApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.util.time.MutableClock

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

import static org.hamcrest.Matchers.hasSize
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ReservationUserPathIntegrationSpec extends AbstractIntegrationSpec {

  private static final Instant BASE_TIME = Instant.parse("2026-05-08T10:00:00Z")
  private static final LocalDateTime FUTURE_START = LocalDateTime.parse("2026-06-01T12:00:00")
  private static final String PASSWORD = "correct horse battery"

  @Autowired
  WebApplicationContext context

  @Autowired
  MutableClock clock

  @Autowired
  JdbcTemplate jdbcTemplate

  MockMvc mockMvc
  AuthApiClient auth
  ReservationApiClient reservations
  WaitlistApiClient waitlist
  TermApiClient terms

  def setup() {
    clock.setInstant(BASE_TIME)
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build()
    auth = new AuthApiClient(mockMvc)
    reservations = new ReservationApiClient(mockMvc)
    waitlist = new WaitlistApiClient(mockMvc)
    terms = new TermApiClient(mockMvc)
  }

  def "guest reserves free place"() {
    given: "An instructor owns a term with free capacity"
        def ownerSession = registerSession()
        def termId = createTerm(ownerSession, uniqueLabel("Free place"), 2, FUTURE_START)
        def email = uniqueEmail()

    when: "A guest reserves a place"
        def created = reservations.reserve(termId, "Anna", "Nowak", email)
        .andExpect(status().isCreated())
        .andExpect(jsonPath('$.type').value('RESERVATION'))
        .andExpect(jsonPath('$.reservation.status').value('CONFIRMED'))
        .andExpect(jsonPath('$.reservation.cancellationToken').isString())
        .andReturn()

    then: "The owner sees the reservation without secret tokens"
        def ownerList = reservations.list(ownerSession, termId)
        .andExpect(status().isOk())
        .andExpect(jsonPath('$', hasSize(1)))
        .andExpect(jsonPath('$[0].cancellationToken').doesNotExist())
        .andExpect(jsonPath('$[0].waitlistConfirmationToken').doesNotExist())
        .andReturn()
        reservationByEmail(ownerList, email).id == json(created, '$.reservation.id')
  }

  def "full term creates waitlist entry for next participant"() {
    given: "A single-seat term exists"
        def ownerSession = registerSession()
        def termId = createTerm(ownerSession, uniqueLabel("Waitlist term"), 1, FUTURE_START)

    when: "Two guests reserve the same term"
        reservations.reserve(termId, "Anna", "Nowak", uniqueEmail())
        .andExpect(status().isCreated())
        .andExpect(jsonPath('$.type').value('RESERVATION'))
        .andExpect(jsonPath('$.reservation.status').value('CONFIRMED'))
        def waitlistedEmail = uniqueEmail()
        def waitlisted = reservations.reserve(termId, "Jan", "Kowalski", waitlistedEmail)

    then: "The second guest receives a waitlist entry"
        waitlisted
        .andExpect(status().isCreated())
        .andExpect(jsonPath('$.type').value('WAITLIST_ENTRY'))
        .andExpect(jsonPath('$.waitlistEntry.position').value(1))
        .andExpect(jsonPath('$.waitlistEntry.cancellationToken').isString())

    and: "The owner sees waitlist entries separately from reservations"
        reservations.list(ownerSession, termId)
        .andExpect(status().isOk())
        .andExpect(jsonPath('$', hasSize(1)))
        waitlist.list(ownerSession, termId)
        .andExpect(status().isOk())
        .andExpect(jsonPath('$', hasSize(1)))
        .andExpect(jsonPath('$[0].email').value(waitlistedEmail.toLowerCase(Locale.ROOT)))
  }

  def "rejects duplicate active reservation email"() {
    given: "A participant already reserved a term"
        def ownerSession = registerSession()
        def termId = createTerm(ownerSession, uniqueLabel("Duplicate term"), 2, FUTURE_START)
        def email = uniqueEmail()
        reservations.reserve(termId, "Anna", "Nowak", email).andExpect(status().isCreated())

    when: "The same email is used again with different case"
        def duplicate = reservations.reserve(termId, "Anna", "Nowak", email.toUpperCase(Locale.ROOT))

    then: "The API rejects duplicate active reservation"
        duplicate
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath('$.title').value('Invalid booking request'))
  }

  def "rejects reservation after term started"() {
    given: "A term exists"
        def ownerSession = registerSession()
        def termId = createTerm(ownerSession, uniqueLabel("Started term"), 1, FUTURE_START)

    when: "The current time is after term start"
        clock.setInstant(Instant.parse("2026-06-01T10:00:01Z"))

    then: "New reservation is rejected"
        reservations.reserve(termId, "Anna", "Nowak", uniqueEmail())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath('$.title').value('Invalid booking request'))
  }

  def "participant cancels waitlist entry and positions are compacted"() {
    given: "A full term has two waitlist entries"
        def ownerSession = registerSession()
        def termId = createTerm(ownerSession, uniqueLabel("Waitlist cancel term"), 1, FUTURE_START)
        reservations.reserve(termId, "Anna", "Nowak", uniqueEmail()).andExpect(status().isCreated())
        def firstWaitlisted = reservations.reserve(termId, "Jan", "Kowalski", uniqueEmail()).andExpect(status().isCreated()).andReturn()
        def secondWaitlistedEmail = uniqueEmail()
        reservations.reserve(termId, "Ewa", "Zielinska", secondWaitlistedEmail).andExpect(status().isCreated())

    when: "The first waitlisted participant cancels with the public token"
        waitlist.cancelByParticipant(json(firstWaitlisted, '$.waitlistEntry.cancellationToken') as String)
        .andExpect(status().isOk())

    then: "The remaining waitlist entry moves to first position"
        waitlist.list(ownerSession, termId)
        .andExpect(status().isOk())
        .andExpect(jsonPath('$', hasSize(1)))
        .andExpect(jsonPath('$[0].email').value(secondWaitlistedEmail.toLowerCase(Locale.ROOT)))
        .andExpect(jsonPath('$[0].position').value(1))
  }

  def "owner removes waitlist entry and positions are compacted"() {
    given: "A full term has two waitlist entries"
        def ownerSession = registerSession()
        def termId = createTerm(ownerSession, uniqueLabel("Waitlist owner remove term"), 1, FUTURE_START)
        reservations.reserve(termId, "Anna", "Nowak", uniqueEmail()).andExpect(status().isCreated())
        def firstWaitlisted = reservations.reserve(termId, "Jan", "Kowalski", uniqueEmail()).andExpect(status().isCreated()).andReturn()
        def secondWaitlistedEmail = uniqueEmail()
        reservations.reserve(termId, "Ewa", "Zielinska", secondWaitlistedEmail).andExpect(status().isCreated())
        def firstWaitlistedId = UUID.fromString(json(firstWaitlisted, '$.waitlistEntry.id') as String)

    when: "The owner removes the first waitlist entry"
        waitlist.removeByOwner(ownerSession, termId, firstWaitlistedId)
        .andExpect(status().isOk())

    then: "The remaining waitlist entry moves to first position"
        waitlist.list(ownerSession, termId)
        .andExpect(status().isOk())
        .andExpect(jsonPath('$', hasSize(1)))
        .andExpect(jsonPath('$[0].email').value(secondWaitlistedEmail.toLowerCase(Locale.ROOT)))
        .andExpect(jsonPath('$[0].position').value(1))
  }

  def "participant cancels before deadline and first waitlisted gets offer"() {
    given: "A full term has one waitlisted participant"
        def ownerSession = registerSession()
        def termId = createTerm(ownerSession, uniqueLabel("Cancel term"), 1, FUTURE_START)
        def confirmedEmail = uniqueEmail()
        def waitlistedEmail = uniqueEmail()
        def confirmed = reservations.reserve(termId, "Anna", "Nowak", confirmedEmail).andExpect(status().isCreated()).andReturn()
        reservations.reserve(termId, "Jan", "Kowalski", waitlistedEmail).andExpect(status().isCreated())
        def cancellationToken = json(confirmed, '$.reservation.cancellationToken') as String

    when: "The confirmed participant cancels before deadline"
        reservations.cancelByParticipant(cancellationToken)
        .andExpect(status().isOk())
        .andExpect(jsonPath('$.status').value('CANCELLED_BY_PARTICIPANT'))

    then: "The first waitlisted participant receives a 24-hour offer"
        def ownerList = reservations.list(ownerSession, termId).andExpect(status().isOk()).andReturn()
        reservationByEmail(ownerList, confirmedEmail).status == 'CANCELLED_BY_PARTICIPANT'
        reservationByEmail(ownerList, waitlistedEmail).status == 'WAITLIST_OFFERED'
        Instant.parse(reservationByEmail(ownerList, waitlistedEmail).waitlistOfferExpiresAt as String) == BASE_TIME.plus(Duration.ofHours(24))
  }

  def "rejects participant cancellation after deadline"() {
    given: "A participant has a confirmed reservation"
        def ownerSession = registerSession()
        def termId = createTerm(ownerSession, uniqueLabel("Deadline term"), 1, FUTURE_START)
        def confirmed = reservations.reserve(termId, "Anna", "Nowak", uniqueEmail()).andExpect(status().isCreated()).andReturn()

    when: "The current time is after Warsaw cancellation deadline"
        clock.setInstant(Instant.parse("2026-05-31T00:00:01Z"))

    then: "Cancellation is rejected"
        reservations.cancelByParticipant(json(confirmed, '$.reservation.cancellationToken') as String)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath('$.title').value('Invalid booking request'))
  }

  def "waitlist offer confirmation confirms reservation"() {
    given: "A waitlisted participant has an offer"
        def ownerSession = registerSession()
        def termId = createTerm(ownerSession, uniqueLabel("Offer term"), 1, FUTURE_START)
        def confirmed = reservations.reserve(termId, "Anna", "Nowak", uniqueEmail()).andExpect(status().isCreated()).andReturn()
        def waitlistedEmail = uniqueEmail()
        reservations.reserve(termId, "Jan", "Kowalski", waitlistedEmail).andExpect(status().isCreated())
        reservations.cancelByParticipant(json(confirmed, '$.reservation.cancellationToken') as String).andExpect(status().isOk())
        def confirmationToken = waitlistConfirmationToken(termId, waitlistedEmail)

    when: "The participant confirms the waitlist offer"
        def confirmedOffer = reservations.confirmWaitlistOffer(confirmationToken)

    then: "The reservation becomes confirmed"
        confirmedOffer
        .andExpect(status().isOk())
        .andExpect(jsonPath('$.status').value('CONFIRMED'))

    and: "A wrong waitlist token is rejected"
        reservations.confirmWaitlistOffer("not-a-real-token").andExpect(status().isNotFound())
  }

  def "expired waitlist offer promotes next participant"() {
    given: "Two participants wait and the first has an offer"
        def ownerSession = registerSession()
        def termId = createTerm(ownerSession, uniqueLabel("Expired offer term"), 1, FUTURE_START)
        def confirmed = reservations.reserve(termId, "Anna", "Nowak", uniqueEmail()).andExpect(status().isCreated()).andReturn()
        def firstWaitlistedEmail = uniqueEmail()
        def secondWaitlistedEmail = uniqueEmail()
        reservations.reserve(termId, "Jan", "Kowalski", firstWaitlistedEmail).andExpect(status().isCreated())
        reservations.reserve(termId, "Ewa", "Zielinska", secondWaitlistedEmail).andExpect(status().isCreated())
        reservations.cancelByParticipant(json(confirmed, '$.reservation.cancellationToken') as String).andExpect(status().isOk())

    when: "The instructor expires stale waitlist offers after TTL"
        clock.plus(Duration.ofHours(24).plusSeconds(1))
        reservations.expireWaitlistOffers(ownerSession, termId)
        .andExpect(status().isOk())
        .andExpect(jsonPath('$.expiredCount').value(1))

    then: "The stale offer expires and the next waitlisted participant gets an offer"
        def ownerList = reservations.list(ownerSession, termId).andExpect(status().isOk()).andReturn()
        reservationByEmail(ownerList, firstWaitlistedEmail).status == 'WAITLIST_OFFER_EXPIRED'
        reservationByEmail(ownerList, secondWaitlistedEmail).status == 'WAITLIST_OFFERED'
  }

  def "instructor cancellation promotes waitlisted participant"() {
    given: "A full term has one waitlisted participant"
        def ownerSession = registerSession()
        def termId = createTerm(ownerSession, uniqueLabel("Instructor cancel term"), 1, FUTURE_START)
        def confirmedEmail = uniqueEmail()
        def waitlistedEmail = uniqueEmail()
        def confirmed = reservations.reserve(termId, "Anna", "Nowak", confirmedEmail).andExpect(status().isCreated()).andReturn()
        reservations.reserve(termId, "Jan", "Kowalski", waitlistedEmail).andExpect(status().isCreated())
        def reservationId = UUID.fromString(json(confirmed, '$.reservation.id') as String)

    when: "The instructor cancels the confirmed reservation"
        reservations.cancelByInstructor(ownerSession, termId, reservationId)
        .andExpect(status().isOk())
        .andExpect(jsonPath('$.status').value('CANCELLED_BY_INSTRUCTOR'))

    then: "The waitlisted participant receives an offer"
        def ownerList = reservations.list(ownerSession, termId).andExpect(status().isOk()).andReturn()
        reservationByEmail(ownerList, confirmedEmail).status == 'CANCELLED_BY_INSTRUCTOR'
        reservationByEmail(ownerList, waitlistedEmail).status == 'WAITLIST_OFFERED'
  }

  def "non-owner cannot manage reservations for another instructor term"() {
    given: "A term owned by another instructor has a reservation"
        def ownerSession = registerSession()
        def otherSession = registerSession()
        def termId = createTerm(ownerSession, uniqueLabel("Private reservation term"), 1, FUTURE_START)
        def reservation = reservations.reserve(termId, "Anna", "Nowak", uniqueEmail())
        .andExpect(status().isCreated())
        .andReturn()
        def reservationId = UUID.fromString(json(reservation, '$.reservation.id') as String)

    expect: "The other instructor cannot manage reservations for the owner term"
        reservations.list(otherSession, termId).andExpect(status().isNotFound())
        reservations.cancelByInstructor(otherSession, termId, reservationId).andExpect(status().isNotFound())
        reservations.expireWaitlistOffers(otherSession, termId).andExpect(status().isNotFound())
        waitlist.list(otherSession, termId).andExpect(status().isNotFound())
  }

  def "reserve with account creation starts session"() {
    given: "A public term exists"
        def ownerSession = registerSession()
        def termId = createTerm(ownerSession, uniqueLabel("Account term"), 2, FUTURE_START)

    when: "A guest reserves a place and creates an account"
        def result = reservations.reserve(
          null,
          termId,
          "Anna",
          "Nowak",
          uniqueEmail(),
          true,
          uniqueUsername(),
          PASSWORD
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath('$.type').value('RESERVATION'))
        .andExpect(jsonPath('$.reservation.status').value('CONFIRMED'))
        .andExpect(jsonPath('$.reservation.participantUserId').isString())
        .andReturn()

    then: "The response session is authenticated"
        def session = result.request.getSession(false) as MockHttpSession
        session != null
        auth.me(session).andExpect(status().isOk())
  }

  def "authenticated user cannot create another account while reserving"() {
    given: "A signed-in user and a public term exist"
        def ownerSession = registerSession()
        def participantSession = registerSession()
        def termId = createTerm(ownerSession, uniqueLabel("Authenticated account term"), 2, FUTURE_START)

    expect: "The reservation request is rejected before another account is created"
        reservations.reserve(
          participantSession,
          termId,
          "Anna",
          "Nowak",
          uniqueEmail(),
          true,
          uniqueUsername(),
          PASSWORD
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath('$.title').value('Invalid booking request'))
  }

  private MockHttpSession registerSession() {
    def result = auth.register(uniqueEmail(), uniqueUsername(), PASSWORD).andExpect(status().isCreated()).andReturn()
    result.request.getSession(false) as MockHttpSession
  }

  private UUID createTerm(MockHttpSession session, String name, int capacity, LocalDateTime startsAt) {
    def result = terms.create(session, name, capacity, startsAt).andExpect(status().isCreated()).andReturn()
    UUID.fromString(json(result, '$.id') as String)
  }

  private String waitlistConfirmationToken(UUID termId, String email) {
    jdbcTemplate.queryForObject(
      "select waitlist_confirmation_token from booking_reservations where term_id = ? and email = ?",
      String.class,
      termId,
      email.toLowerCase(Locale.ROOT)
    )
  }

  private static Map<String, Object> reservationByEmail(MvcResult result, String email) {
    def reservation = (json(result, '$') as List<Map<String, Object>>).find { reservation -> reservation.email == email.toLowerCase(Locale.ROOT) }
    assert reservation != null
    reservation
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
