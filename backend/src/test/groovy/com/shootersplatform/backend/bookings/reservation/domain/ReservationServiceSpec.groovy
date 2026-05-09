package com.shootersplatform.backend.bookings.reservation.domain


import com.shootersplatform.backend.bookings.term.domain.InMemoryTermRepository
import com.shootersplatform.backend.bookings.trainingenrollment.domain.InMemoryTrainingEnrollmentRepository
import com.shootersplatform.backend.bookings.location.domain.Location
import com.shootersplatform.backend.bookings.term.domain.Term
import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollment
import com.shootersplatform.backend.identity.domain.UserId
import spock.lang.Specification

import java.time.*

class ReservationServiceSpec extends Specification {

  private UserId owner = UserId.newId()
  private InMemoryTrainingEnrollmentRepository enrollments
  private InMemoryTermRepository terms
  private InMemoryReservationRepository reservations
  private InMemoryReservationNotificationPort notifications
  private ReservationService service
  private MutableClock clock

  def setup() {
    enrollments = new InMemoryTrainingEnrollmentRepository()
    terms = new InMemoryTermRepository()
    reservations = new InMemoryReservationRepository()
    notifications = new InMemoryReservationNotificationPort()
    clock = new MutableClock(Instant.parse("2026-05-08T10:00:00Z"))
    service = new ReservationService(terms, reservations, notifications, clock)
  }

  def "creates training enrollment and term by copying editable fields"() {
    when: "The instructor creates an enrollment and a concrete term"
        def enrollment = createEnrollment(" Basic pistol ", " Safety and stance ", 4, 2, 90)
        def term = terms.save(termFrom(enrollment, LocalDateTime.parse("2026-06-01T12:30:00")))

    then: "The term keeps a copied snapshot of the enrollment fields"
        enrollment.name() == "Basic pistol"
        enrollment.description() == "Safety and stance"
        term.name() == "Basic pistol"
        term.location().address() == "Range Street 1"
        term.capacity() == 4
        term.cancellationDeadlineDays() == 2
        term.durationMinutes() == 90
  }

  def "confirms reservation while term has free capacity and waitlists once capacity is full"() {
    given: "A term with a single available place exists"
        def term = singleSeatTerm()

    when: "Two participants register"
        def first = service.createReservation(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111")
        def second = service.createReservation(term.id(), null, "Jan", "Kowalski", "jan@example.com", "+48222222222")

    then: "The first participant is confirmed and the second is waitlisted"
        first.status() == ReservationStatus.CONFIRMED
        second.status() == ReservationStatus.WAITLISTED
        second.waitlistPosition() == 1
        notifications.confirmed()*.email()*.value() == ["anna@example.com"]
  }

  def "rejects duplicate active reservation email for the same term"() {
    given: "A participant already has an active reservation"
        def term = singleSeatTerm()
        service.createReservation(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111")

    when: "The same email is used again"
        service.createReservation(term.id(), null, "Anna", "Nowak", "ANNA@example.com", "+48111111111")

    then: "The domain rejects the duplicate"
        thrown(ReservationValidationException)
  }

  def "participant cancellation promotes first waitlisted reservation to offered"() {
    given: "A full term has one participant on the waitlist"
        def term = singleSeatTerm()
        def confirmed = service.createReservation(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111")
        service.createReservation(term.id(), null, "Jan", "Kowalski", "jan@example.com", "+48222222222")

    when: "The confirmed participant cancels"
        def cancelled = service.cancelByParticipant(confirmed.cancellationToken())
        def reservationsForTerm = reservations.findByTerm(term.id())

    then: "The first waitlisted participant receives a time-limited offer"
        cancelled.status() == ReservationStatus.CANCELLED_BY_PARTICIPANT
        statusByEmail(reservationsForTerm) == [
          "anna@example.com": ReservationStatus.CANCELLED_BY_PARTICIPANT,
          "jan@example.com" : ReservationStatus.WAITLIST_OFFERED
        ]
        notifications.waitlistOffers().first().waitlistConfirmationToken() != null
        notifications.waitlistOffers().first().waitlistOfferExpiresAt() == Instant.parse("2026-05-09T10:00:00Z")
  }

  def "waitlist confirmation link confirms the offered reservation"() {
    given: "A waitlisted participant has received an offer"
        def term = singleSeatTerm()
        def confirmed = service.createReservation(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111")
        service.createReservation(term.id(), null, "Jan", "Kowalski", "jan@example.com", "+48222222222")
        service.cancelByParticipant(confirmed.cancellationToken())
        def token = notifications.waitlistOffers().first().waitlistConfirmationToken()

    when: "The participant confirms the offer"
        def promoted = service.confirmWaitlistOffer(token)

    then: "The reservation becomes confirmed"
        promoted.status() == ReservationStatus.CONFIRMED
        promoted.waitlistConfirmationToken() == null
  }

  def "expired waitlist offer is closed and next waitlisted reservation is offered"() {
    given: "Two participants are waiting and the first receives an offer"
        def term = singleSeatTerm()
        def confirmed = service.createReservation(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111")
        service.createReservation(term.id(), null, "Jan", "Kowalski", "jan@example.com", "+48222222222")
        service.createReservation(term.id(), null, "Ewa", "Zielinska", "ewa@example.com", "+48333333333")
        service.cancelByParticipant(confirmed.cancellationToken())

    when: "The instructor expires stale offers after the link TTL"
        clock.instant = Instant.parse("2026-05-09T10:01:00Z")
        def expired = service.expireWaitlistOffers(owner, term.id())

    then: "The stale offer is expired and the next waitlisted reservation receives an offer"
        expired == 1
        statusByEmail(reservations.findByTerm(term.id())) == [
          "anna@example.com": ReservationStatus.CANCELLED_BY_PARTICIPANT,
          "jan@example.com" : ReservationStatus.WAITLIST_OFFER_EXPIRED,
          "ewa@example.com" : ReservationStatus.WAITLIST_OFFERED
        ]
  }

  def "participant cannot cancel after configured cancellation deadline"() {
    given: "A term starts too soon to cancel"
        def term = singleSeatTerm()
        def confirmed = service.createReservation(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111")
        clock.instant = Instant.parse("2026-05-31T00:00:01Z")

    when: "The participant tries to cancel after the deadline"
        service.cancelByParticipant(confirmed.cancellationToken())

    then: "The cancellation is rejected"
        thrown(ReservationValidationException)
  }

  def "rejects reservation after term has started"() {
    given: "A term has already started"
        def term = singleSeatTerm()
        clock.instant = Instant.parse("2026-06-01T10:00:01Z")

    when: "A participant tries to reserve it"
        service.createReservation(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111")

    then: "The domain rejects late reservations"
        thrown(ReservationValidationException)
  }

  def "participant cancellation deadline uses Warsaw midnight before start date"() {
    given: "A term starts at noon Warsaw time with one cancellation day"
        def term = singleSeatTerm()
        def confirmed = service.createReservation(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111")

    when: "The participant cancels exactly at the configured Warsaw midnight deadline"
        clock.instant = Instant.parse("2026-05-30T22:00:00Z")
        def cancelled = service.cancelByParticipant(confirmed.cancellationToken())

    then: "The cancellation is still accepted"
        cancelled.status() == ReservationStatus.CANCELLED_BY_PARTICIPANT
  }

  private Term singleSeatTerm() {
    def enrollment = createEnrollment("Basic pistol", "", 1, 1, 60)
    terms.save(termFrom(enrollment, LocalDateTime.parse("2026-06-01T12:00:00")))
  }

  private TrainingEnrollment createEnrollment(String name, String description, int capacity, int cancellationDeadlineDays, int durationMinutes) {
    enrollments.save(TrainingEnrollment.create(owner, name, description, location(), capacity, cancellationDeadlineDays, durationMinutes, clock.instant()))
  }

  private Term termFrom(TrainingEnrollment enrollment, LocalDateTime startsAt) {
    Term.create(
      owner,
      enrollment.name(),
      enrollment.description(),
      enrollment.location(),
      enrollment.capacity(),
      enrollment.cancellationDeadlineDays(),
      enrollment.durationMinutes(),
      startsAt,
      clock.instant()
    )
  }

  private static Location location() {
    new Location("Range A", "Range Street 1", 52.2297d, 21.0122d)
  }

  private static Map<String, ReservationStatus> statusByEmail(List<Reservation> reservations) {
    reservations.collectEntries { reservation -> [(reservation.email().value()): reservation.status()] }
  }

  private static class MutableClock extends Clock {
    Instant instant

    MutableClock(Instant instant) {
      this.instant = instant
    }

    @Override
    ZoneOffset getZone() {
      ZoneOffset.UTC
    }

    @Override
    Clock withZone(ZoneId zone) {
      this
    }

    @Override
    Instant instant() {
      instant
    }
  }
}
