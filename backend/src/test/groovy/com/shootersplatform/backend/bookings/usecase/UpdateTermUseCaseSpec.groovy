package com.shootersplatform.backend.bookings.usecase

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

class UpdateTermUseCaseSpec extends Specification {

  private UserId owner = UserId.newId()
  private InMemoryTermRepository terms
  private UpdateTermUseCase updateTerm

  def setup() {
    def clock = Clock.fixed(Instant.parse("2026-05-08T10:00:00Z"), ZoneOffset.UTC)
    terms = new InMemoryTermRepository()
    def reservations = new InMemoryReservationRepository()
    def waitlist = new InMemoryWaitlistRepository()
    updateTerm = new UpdateTermUseCase(
      new TermService(terms, clock),
      new ReservationService(terms, reservations, waitlist, new WaitlistService(terms, waitlist, clock), new InMemoryReservationNotificationPort(), clock)
    )
  }

  def "updates term editable fields while preserving original capacity"() {
    given: "A term has a fixed capacity"
        def term = termWithCapacity(3)

    when: "The instructor updates editable term details"
        def updated = updateTerm.update(owner, term.id(), "Advanced pistol", "Updated", location(), 2, 90, LocalDateTime.parse("2026-06-01T14:00:00")).term()

    then: "The original capacity is kept"
        updated.name() == "Advanced pistol"
        updated.capacity() == 3
        updated.cancellationDeadlineDays() == 2
        updated.durationMinutes() == 90
  }

  private Term termWithCapacity(int capacity) {
    terms.save(Term.create(owner, "Basic pistol", "", location(), capacity, 1, 60, LocalDateTime.parse("2026-06-01T12:00:00"), Instant.parse("2026-05-08T10:00:00Z")))
  }

  private static Location location() {
    new Location("Range A", "Range Street 1", 52.2297d, 21.0122d)
  }
}
