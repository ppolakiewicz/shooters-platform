package com.shootersplatform.backend.bookings.term.usecase

import com.shootersplatform.backend.bookings.reservation.domain.InMemoryReservationNotificationPort
import com.shootersplatform.backend.bookings.reservation.domain.InMemoryReservationRepository
import com.shootersplatform.backend.bookings.term.domain.InMemoryTermRepository
import com.shootersplatform.backend.bookings.location.domain.Location
import com.shootersplatform.backend.bookings.reservation.domain.ReservationService
import com.shootersplatform.backend.bookings.term.domain.Term
import com.shootersplatform.backend.bookings.term.domain.TermService
import com.shootersplatform.backend.bookings.term.domain.TermValidationException
import com.shootersplatform.backend.identity.domain.UserId
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class UpdateTermUseCaseSpec extends Specification {

  private UserId owner = UserId.newId()
  private InMemoryTermRepository terms
  private InMemoryReservationRepository reservations
  private ReservationService reservationService
  private UpdateTermUseCase updateTerm

  def setup() {
    def clock = Clock.fixed(Instant.parse("2026-05-08T10:00:00Z"), ZoneOffset.UTC)
    terms = new InMemoryTermRepository()
    reservations = new InMemoryReservationRepository()
    reservationService = new ReservationService(terms, reservations, new InMemoryReservationNotificationPort(), clock)
    updateTerm = new UpdateTermUseCase(new TermService(terms, clock), reservations)
  }

  def "updates term when capacity still covers occupied places"() {
    given: "A term has one confirmed participant"
        def term = termWithCapacity(3)
        reservationService.createReservation(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111")

    when: "The instructor lowers capacity to the occupied count"
        def updated = updateTerm.update(owner, term.id(), "Basic pistol", "", location(), 1, 1, 60, LocalDateTime.parse("2026-06-01T12:00:00"))

    then: "The update is accepted"
        updated.capacity() == 1
  }

  def "rejects term update below occupied places"() {
    given: "A term has two occupied places"
        def term = termWithCapacity(2)
        reservationService.createReservation(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111")
        def confirmed = reservationService.createReservation(term.id(), null, "Jan", "Kowalski", "jan@example.com", "+48222222222")

    when: "The instructor lowers capacity below occupied places"
        updateTerm.update(owner, term.id(), "Basic pistol", "", location(), 1, 1, 60, LocalDateTime.parse("2026-06-01T12:00:00"))

    then: "The update is rejected"
        thrown(TermValidationException)
        reservations.findByIdAndTerm(confirmed.id(), term.id()).orElseThrow().status().name() == "CONFIRMED"
  }

  private Term termWithCapacity(int capacity) {
    terms.save(Term.create(owner, "Basic pistol", "", location(), capacity, 1, 60, LocalDateTime.parse("2026-06-01T12:00:00"), Instant.parse("2026-05-08T10:00:00Z")))
  }

  private static Location location() {
    new Location("Range A", "Range Street 1", 52.2297d, 21.0122d)
  }
}
