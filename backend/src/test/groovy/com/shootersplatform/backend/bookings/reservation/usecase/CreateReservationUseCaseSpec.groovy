package com.shootersplatform.backend.bookings.reservation.usecase

import com.shootersplatform.backend.bookings.reservation.domain.InMemoryReservationNotificationPort
import com.shootersplatform.backend.bookings.reservation.domain.InMemoryReservationRepository
import com.shootersplatform.backend.bookings.term.domain.InMemoryTermRepository
import com.shootersplatform.backend.bookings.location.domain.Location
import com.shootersplatform.backend.bookings.reservation.domain.ReservationService
import com.shootersplatform.backend.bookings.reservation.domain.ReservationValidationException
import com.shootersplatform.backend.bookings.term.domain.Term
import com.shootersplatform.backend.bookings.waitlist.domain.InMemoryWaitlistRepository
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistService
import com.shootersplatform.backend.identity.InMemoryLoginRateLimiter
import com.shootersplatform.backend.identity.InMemoryUserAccountRepository
import com.shootersplatform.backend.identity.PlainTextPasswordHasher
import com.shootersplatform.backend.identity.domain.AuthenticatedUser
import com.shootersplatform.backend.identity.domain.IdentityService
import com.shootersplatform.backend.identity.domain.UserId
import com.shootersplatform.backend.identity.usecase.RegisterUserUseCase
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class CreateReservationUseCaseSpec extends Specification {

  private InMemoryUserAccountRepository users
  private InMemoryTermRepository terms
  private InMemoryReservationRepository reservations
  private CreateReservationUseCase useCase

  def setup() {
    def clock = Clock.fixed(Instant.parse("2026-05-08T10:00:00Z"), ZoneOffset.UTC)
    users = new InMemoryUserAccountRepository()
    terms = new InMemoryTermRepository()
    reservations = new InMemoryReservationRepository()
    def waitlist = new InMemoryWaitlistRepository()
    def identity = new IdentityService(users, new PlainTextPasswordHasher(), clock)
    useCase = new CreateReservationUseCase(
      new ReservationService(terms, reservations, waitlist, new WaitlistService(terms, waitlist, clock), new InMemoryReservationNotificationPort(), clock),
      new RegisterUserUseCase(identity, new InMemoryLoginRateLimiter())
    )
  }

  def "creates account and reservation in one use case"() {
    given: "A public term exists"
        def term = createTerm()

    when: "A guest reserves a place and creates an account"
        def result = useCase.create(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111", true, "AnnaNowak", "correct horse battery", "127.0.0.1")

    then: "The reservation is linked to the registered user"
        result.registeredUser() != null
        result.booking().reservation().participantUserId() == result.registeredUser().id()
        users.count() == 1
  }

  def "rejects account creation for authenticated users"() {
    given: "A public term and an authenticated user exist"
        def term = createTerm()
        def user = useCase.create(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111", true, "AnnaNowak", "correct horse battery", "127.0.0.1").registeredUser()

    when: "The authenticated user requests another account during reservation"
        useCase.create(term.id(), user as AuthenticatedUser, "Jan", "Kowalski", "jan@example.com", "+48222222222", true, "JanKowalski", "correct horse battery", "127.0.0.1")

    then: "The use case rejects the request before registering another user"
        thrown(ReservationValidationException)
        users.count() == 1
  }

  def "rejects account creation without credentials before changing state"() {
    given: "A public term exists"
        def term = createTerm()

    when: "A guest asks for account creation without a password"
        useCase.create(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111", true, "AnnaNowak", null, "127.0.0.1")

    then: "No account or reservation is created"
        thrown(ReservationValidationException)
        users.count() == 0
        reservations.findByTerm(term.id()).isEmpty()
  }

  private Term createTerm() {
    terms.save(Term.create(
      UserId.newId(),
      "Basic pistol",
      "",
      new Location("Range A", "Range Street 1", 52.2297d, 21.0122d),
      2,
      1,
      60,
      LocalDateTime.parse("2026-06-01T12:00:00"),
      Instant.parse("2026-05-08T10:00:00Z")
    ))
  }
}
