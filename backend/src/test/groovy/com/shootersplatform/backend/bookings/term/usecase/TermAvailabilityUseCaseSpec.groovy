package com.shootersplatform.backend.bookings.term.usecase

import com.shootersplatform.backend.bookings.location.domain.Location
import com.shootersplatform.backend.bookings.reservation.domain.InMemoryReservationNotificationPort
import com.shootersplatform.backend.bookings.reservation.domain.InMemoryReservationRepository
import com.shootersplatform.backend.bookings.reservation.domain.ReservationService
import com.shootersplatform.backend.bookings.term.domain.InMemoryTermRepository
import com.shootersplatform.backend.bookings.term.domain.Term
import com.shootersplatform.backend.bookings.term.domain.TermService
import com.shootersplatform.backend.bookings.waitlist.domain.InMemoryWaitlistRepository
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistService
import com.shootersplatform.backend.identity.domain.UserId
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class TermAvailabilityUseCaseSpec extends Specification {

  private UserId owner = UserId.newId()
  private InMemoryTermRepository terms
  private ReservationService reservationService
  private TermAvailabilityUseCase useCase

  def setup() {
    def clock = Clock.fixed(Instant.parse("2026-05-08T10:00:00Z"), ZoneOffset.UTC)
    terms = new InMemoryTermRepository()
    def reservations = new InMemoryReservationRepository()
    def waitlist = new InMemoryWaitlistRepository()
    def termService = new TermService(terms, clock)
    reservationService = new ReservationService(terms, reservations, waitlist, new WaitlistService(terms, waitlist, clock), new InMemoryReservationNotificationPort(), clock)
    useCase = new TermAvailabilityUseCase(termService, reservationService)
  }

  def "calculates available places by combining term capacity with occupied reservations"() {
    given: "A public term has three places"
        def term = termWithCapacity(3)

    and: "One place is already reserved"
        reservationService.createReservation(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111")

    when: "The term availability is read"
        def availableTerm = useCase.getPublic(term.id())

    then: "The use case reports remaining places"
        availableTerm.term() == term
        availableTerm.availablePlaces() == 2
  }

  private Term termWithCapacity(int capacity) {
    terms.save(Term.create(owner, "Basic pistol", "", location(), capacity, 1, 60, LocalDateTime.parse("2026-06-01T12:00:00"), Instant.parse("2026-05-08T10:00:00Z")))
  }

  private static Location location() {
    new Location("Range A", "Range Street 1", 52.2297d, 21.0122d)
  }
}
