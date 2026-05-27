package com.shootersplatform.backend.bookings.usecase

import com.shootersplatform.backend.bookings.location.domain.Location
import com.shootersplatform.backend.bookings.reservation.domain.Reservation
import com.shootersplatform.backend.bookings.reservation.domain.ReservationStatus
import com.shootersplatform.backend.bookings.reservation.domain.ReservationValidationException
import com.shootersplatform.backend.bookings.term.domain.Term
import com.shootersplatform.backend.bookings.term.domain.TermNotFoundException
import com.shootersplatform.backend.bookings.term.domain.TermValidationException
import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel
import com.shootersplatform.backend.identity.domain.AuthenticatedUser
import com.shootersplatform.backend.identity.domain.UserId
import spock.lang.Specification
import spock.util.time.MutableClock

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class ReservationUseCasesSpec extends Specification {

    private UserId owner = UserId.newId()
    private UserId otherOwner = UserId.newId()
    private MutableClock clock
    private BookingUseCaseTestContext booking

    def setup() {
        clock = new MutableClock(Instant.parse("2026-05-08T10:00:00Z"), ZoneOffset.UTC)
        booking = new BookingUseCaseTestContext(clock)
    }

    def "creates account and reservation in one use case"() {
        given: "A public term exists"
            def term = createTerm(2)

        when: "A guest reserves a place and creates an account"
            def result = reserve(term, "Anna", "anna@example.com", true)

        then: "The reservation is linked to the registered user"
            result.registeredUser() != null
            booking.listReservations.list(owner, term.id()).first().participantUserId() == result.registeredUser().id()
    }

    def "rejects account creation for authenticated users"() {
        given: "A public term and an authenticated user exist"
            def term = createTerm(2)
            def user = reserve(term, "Anna", "anna@example.com", true).registeredUser()

        when: "The authenticated user requests another account during reservation"
            booking.createReservation.create(term.id(), user as AuthenticatedUser, "Jan", "Kowalski", "jan@example.com", "+48222222222", true, "JanKowalski", "correct horse battery", "127.0.0.1")

        then: "The use case rejects the request"
            thrown(ReservationValidationException)
            booking.listReservations.list(owner, term.id())*.email()*.value() == ["anna@example.com"]
    }

    def "rejects account creation without credentials before creating a reservation"() {
        given: "A public term exists"
            def term = createTerm(2)

        when: "A guest asks for account creation without a password"
            booking.createReservation.create(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111", true, "AnnaNowak", null, "127.0.0.1")

        then: "No reservation is created"
            thrown(ReservationValidationException)
            booking.listReservations.list(owner, term.id()).isEmpty()
    }

    def "confirms reservation while term has free capacity and creates waitlist entry once capacity is full"() {
        given: "A term with a single available place exists"
            def term = createTerm(1)

        when: "Two participants register"
            def first = reserve(term, "Anna", "anna@example.com")
            def second = reserve(term, "Jan", "jan@example.com")

        then: "The first participant is confirmed and the second waits"
            first.booking().reservation().status() == ReservationStatus.CONFIRMED
            second.booking().waitlistEntry().position() == 1
            booking.listReservations.list(owner, term.id())*.email()*.value() == ["anna@example.com"]
            booking.listWaitlistEntries.list(owner, term.id())*.email()*.value() == ["jan@example.com"]
    }

    def "rejects duplicate active reservation and waitlist emails for the same term"() {
        given: "A full term already has one reservation and one waitlist entry"
            def term = createTerm(1)
            reserve(term, "Anna", "anna@example.com")
            reserve(term, "Jan", "jan@example.com")

        when: "The confirmed email is used again"
            reserve(term, "Anna", "ANNA@example.com")

        then: "The duplicate reservation email is rejected"
            thrown(ReservationValidationException)

        when: "The waitlisted email is used again"
            reserve(term, "Jan", "JAN@example.com")

        then: "The duplicate waitlist email is rejected"
            thrown(ReservationValidationException)
    }

    def "participant cancellation promotes first waitlisted reservation to offered"() {
        given: "A full term has one participant on the waitlist"
            def term = createTerm(1)
            def confirmed = reserve(term, "Anna", "anna@example.com").booking().reservation()
            reserve(term, "Jan", "jan@example.com")

        when: "The confirmed participant cancels"
            def cancelled = booking.cancelReservationByParticipant.cancel(confirmed.cancellationToken())

        then: "The first waitlisted participant receives a time-limited offer"
            cancelled.status() == ReservationStatus.CANCELLED_BY_PARTICIPANT
            statusByEmail(booking.listReservations.list(owner, term.id())) == [
                    "anna@example.com": ReservationStatus.CANCELLED_BY_PARTICIPANT,
                    "jan@example.com" : ReservationStatus.WAITLIST_OFFERED
            ]
            booking.listWaitlistEntries.list(owner, term.id()).isEmpty()
    }

    def "instructor cancellation promotes first waitlisted reservation to offered"() {
        given: "A full term has one participant on the waitlist"
            def term = createTerm(1)
            def confirmed = reserve(term, "Anna", "anna@example.com").booking().reservation()
            reserve(term, "Jan", "jan@example.com")

        when: "The instructor cancels the confirmed reservation"
            def cancelled = booking.cancelReservationByInstructor.cancel(owner, term.id(), confirmed.id())

        then: "The first waitlisted participant receives a time-limited offer"
            cancelled.status() == ReservationStatus.CANCELLED_BY_INSTRUCTOR
            statusByEmail(booking.listReservations.list(owner, term.id())) == [
                    "anna@example.com": ReservationStatus.CANCELLED_BY_INSTRUCTOR,
                    "jan@example.com" : ReservationStatus.WAITLIST_OFFERED
            ]
    }

    def "instructor cancellation requires ownership of the term"() {
        given: "A confirmed reservation exists"
            def term = createTerm(1)
            def confirmed = reserve(term, "Anna", "anna@example.com").booking().reservation()

        when: "Another owner tries to cancel it"
            booking.cancelReservationByInstructor.cancel(otherOwner, term.id(), confirmed.id())

        then: "The term is hidden from the other owner"
            thrown(TermNotFoundException)
    }

    def "waitlist confirmation link confirms the offered reservation"() {
        given: "A waitlisted participant has received an offer"
            def term = createTerm(1)
            def confirmed = reserve(term, "Anna", "anna@example.com").booking().reservation()
            reserve(term, "Jan", "jan@example.com")
            booking.cancelReservationByParticipant.cancel(confirmed.cancellationToken())
            def offered = booking.listReservations.list(owner, term.id()).find { it.email().value() == "jan@example.com" }

        when: "The participant confirms the offer"
            def promoted = booking.confirmWaitlistOffer.confirm(offered.waitlistConfirmationToken())

        then: "The reservation becomes confirmed"
            promoted.status() == ReservationStatus.CONFIRMED
            promoted.waitlistConfirmationToken() == null
            booking.listReservations.list(owner, term.id()).find { it.email().value() == "jan@example.com" }.status() == ReservationStatus.CONFIRMED
    }

    def "expired waitlist offer is closed and next waitlisted reservation is offered"() {
        given: "Two participants are waiting and the first receives an offer"
            def term = createTerm(1)
            def confirmed = reserve(term, "Anna", "anna@example.com").booking().reservation()
            reserve(term, "Jan", "jan@example.com")
            reserve(term, "Ewa", "ewa@example.com")
            booking.cancelReservationByParticipant.cancel(confirmed.cancellationToken())

        when: "The instructor expires stale offers after the link TTL"
            clock.instant = Instant.parse("2026-05-09T10:01:00Z")
            def expired = booking.expireWaitlistOffers.expire(owner, term.id())

        then: "The stale offer is expired and the next waitlisted reservation receives an offer"
            expired == 1
            statusByEmail(booking.listReservations.list(owner, term.id())) == [
                    "anna@example.com": ReservationStatus.CANCELLED_BY_PARTICIPANT,
                    "jan@example.com" : ReservationStatus.WAITLIST_OFFER_EXPIRED,
                    "ewa@example.com" : ReservationStatus.WAITLIST_OFFERED
            ]
    }

    def "participant cannot cancel after configured cancellation deadline"() {
        given: "A term starts too soon to cancel"
            def term = createTerm(1)
            def confirmed = reserve(term, "Anna", "anna@example.com").booking().reservation()
            clock.instant = Instant.parse("2026-05-31T00:00:01Z")

        when: "The participant tries to cancel after the deadline"
            booking.cancelReservationByParticipant.cancel(confirmed.cancellationToken())

        then: "The cancellation is rejected and the reservation stays confirmed"
            thrown(TermValidationException)
            booking.listReservations.list(owner, term.id()).first().status() == ReservationStatus.CONFIRMED
    }

    def "listing reservations requires term ownership"() {
        given: "A term has a reservation"
            def term = createTerm(1)
            reserve(term, "Anna", "anna@example.com")

        when: "Another owner tries to list reservations"
            booking.listReservations.list(otherOwner, term.id())

        then: "The term is hidden from the other owner"
            thrown(TermNotFoundException)
    }

    private Term createTerm(int capacity) {
        booking.createTerm.create(owner, "Basic pistol", "", TrainingLevel.BASIC, location(), capacity, 1, 60, LocalDateTime.parse("2026-06-01T12:00:00")).term()
    }

    private CreateReservationResult reserve(Term term, String firstName, String email, boolean createAccount = false) {
        booking.createReservation.create(
                term.id(),
                null,
                firstName,
                "Nowak",
                email,
                "+48111111111",
                createAccount,
                createAccount ? firstName + "Nowak" : null,
                createAccount ? "correct horse battery" : null,
                "127.0.0.1"
        )
    }

    private static Map<String, ReservationStatus> statusByEmail(List<Reservation> reservations) {
        reservations.collectEntries { reservation -> [(reservation.email().value()): reservation.status()] }
    }

    private static Location location() {
        new Location("Range A", "Range Street 1", 52.2297d, 21.0122d)
    }
}
