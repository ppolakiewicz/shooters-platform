package com.shootersplatform.backend.bookings.usecase

import com.shootersplatform.backend.bookings.location.domain.Location
import com.shootersplatform.backend.bookings.notification.domain.BookingNotificationService
import com.shootersplatform.backend.bookings.notification.domain.InMemoryBookingNotificationGateway
import com.shootersplatform.backend.bookings.reservation.domain.InMemoryReservationRepository
import com.shootersplatform.backend.bookings.reservation.domain.Reservation
import com.shootersplatform.backend.bookings.reservation.domain.ReservationService
import com.shootersplatform.backend.bookings.reservation.domain.ReservationStatus
import com.shootersplatform.backend.bookings.reservation.domain.ReservationValidationException
import com.shootersplatform.backend.bookings.term.domain.InMemoryTermRepository
import com.shootersplatform.backend.bookings.term.domain.Term
import com.shootersplatform.backend.bookings.term.domain.TermService
import com.shootersplatform.backend.bookings.term.domain.TermValidationException
import com.shootersplatform.backend.bookings.waitlist.domain.InMemoryWaitlistRepository
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistService
import com.shootersplatform.backend.identity.InMemoryLoginRateLimiter
import com.shootersplatform.backend.identity.InMemoryUserAccountRepository
import com.shootersplatform.backend.identity.PlainTextPasswordHasher
import com.shootersplatform.backend.identity.domain.AuthenticatedUser
import com.shootersplatform.backend.identity.domain.IdentityService
import com.shootersplatform.backend.identity.domain.UserId
import com.shootersplatform.backend.identity.usecase.IdentityUseCaseFactory
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class CreateReservationUseCaseSpec extends Specification {

    private UserId owner = UserId.newId()
    private InMemoryUserAccountRepository users
    private InMemoryTermRepository terms
    private InMemoryReservationRepository reservations
    private InMemoryWaitlistRepository waitlist
    private InMemoryBookingNotificationGateway notifications
    private MutableClock clock
    private CreateReservationUseCase createReservation
    private CancelReservationByParticipantUseCase cancelReservation
    private ConfirmWaitlistOfferUseCase confirmWaitlistOffer
    private ExpireWaitlistOffersUseCase expireWaitlistOffers

    def setup() {
        clock = new MutableClock(Instant.parse("2026-05-08T10:00:00Z"))
        users = new InMemoryUserAccountRepository()
        terms = new InMemoryTermRepository()
        reservations = new InMemoryReservationRepository()
        waitlist = new InMemoryWaitlistRepository()
        notifications = new InMemoryBookingNotificationGateway()

        def identity = new IdentityService(users, new PlainTextPasswordHasher(), clock)
        def termService = new TermService(terms, clock)
        def reservationService = new ReservationService(reservations, clock)
        def waitlistService = new WaitlistService(waitlist, clock)
        def notificationService = new BookingNotificationService(notifications)
        def promotion = new WaitlistPromotionCoordinator(reservationService, waitlistService, notificationService, clock)

        createReservation = new CreateReservationUseCase(
                reservationService,
                termService,
                waitlistService,
                notificationService,
                IdentityUseCaseFactory.registerUser(identity, new InMemoryLoginRateLimiter())
        )
        cancelReservation = new CancelReservationByParticipantUseCase(reservationService, termService, promotion)
        confirmWaitlistOffer = new ConfirmWaitlistOfferUseCase(reservationService, termService)
        expireWaitlistOffers = new ExpireWaitlistOffersUseCase(reservationService, termService, promotion)
    }

    def "creates account and reservation in one use case"() {
        given: "A public term exists"
            def term = createTerm(2)

        when: "A guest reserves a place and creates an account"
            def result = createReservation.create(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111", true, "AnnaNowak", "correct horse battery", "127.0.0.1")

        then: "The reservation is linked to the registered user"
            result.registeredUser() != null
            result.booking().reservation().participantUserId() == result.registeredUser().id()
            users.count() == 1
    }

    def "rejects account creation for authenticated users"() {
        given: "A public term and an authenticated user exist"
            def term = createTerm(2)
            def user = reserve(term, "Anna", "anna@example.com", true).registeredUser()

        when: "The authenticated user requests another account during reservation"
            createReservation.create(term.id(), user as AuthenticatedUser, "Jan", "Kowalski", "jan@example.com", "+48222222222", true, "JanKowalski", "correct horse battery", "127.0.0.1")

        then: "The use case rejects the request before registering another user"
            thrown(ReservationValidationException)
            users.count() == 1
    }

    def "rejects account creation without credentials before changing state"() {
        given: "A public term exists"
            def term = createTerm(2)

        when: "A guest asks for account creation without a password"
            createReservation.create(term.id(), null, "Anna", "Nowak", "anna@example.com", "+48111111111", true, "AnnaNowak", null, "127.0.0.1")

        then: "No account or reservation is created"
            thrown(ReservationValidationException)
            users.count() == 0
            reservations.findByTerm(term.id()).isEmpty()
    }

    def "confirms reservation while term has free capacity and creates waitlist entry once capacity is full"() {
        given: "A term with a single available place exists"
            def term = createTerm(1)

        when: "Two participants register"
            def first = reserve(term, "Anna", "anna@example.com")
            def second = reserve(term, "Jan", "jan@example.com")

        then: "The first participant is confirmed and the second has a waitlist entry"
            first.booking().reservation().status() == ReservationStatus.CONFIRMED
            second.booking().waitlistEntry().position() == 1
            reservations.findByTerm(term.id())*.email()*.value() == ["anna@example.com"]
            waitlist.findByTerm(term.id())*.email()*.value() == ["jan@example.com"]
            notifications.confirmedReservations()*.reservation()*.email()*.value() == ["anna@example.com"]
    }

    def "rejects duplicate active reservation email for the same term"() {
        given: "A participant already has an active reservation"
            def term = createTerm(1)
            reserve(term, "Anna", "anna@example.com")

        when: "The same email is used again"
            reserve(term, "Anna", "ANNA@example.com")

        then: "The use case rejects the duplicate"
            thrown(ReservationValidationException)
    }

    def "rejects duplicate waitlist email for the same term"() {
        given: "A participant already has a waitlist entry"
            def term = createTerm(1)
            reserve(term, "Anna", "anna@example.com")
            reserve(term, "Jan", "jan@example.com")

        when: "The waitlisted email is used again"
            reserve(term, "Jan", "JAN@example.com")

        then: "The use case rejects the duplicate"
            thrown(ReservationValidationException)
    }

    def "participant cancellation promotes first waitlisted reservation to offered"() {
        given: "A full term has one participant on the waitlist"
            def term = createTerm(1)
            def confirmed = reserve(term, "Anna", "anna@example.com").booking().reservation()
            reserve(term, "Jan", "jan@example.com")

        when: "The confirmed participant cancels"
            def cancelled = cancelReservation.cancel(confirmed.cancellationToken())

        then: "The first waitlisted participant receives a time-limited offer"
            cancelled.status() == ReservationStatus.CANCELLED_BY_PARTICIPANT
            statusByEmail(reservations.findByTerm(term.id())) == [
                    "anna@example.com": ReservationStatus.CANCELLED_BY_PARTICIPANT,
                    "jan@example.com" : ReservationStatus.WAITLIST_OFFERED
            ]
            notifications.waitlistOffers().first().reservation().waitlistConfirmationToken() != null
            notifications.waitlistOffers().first().reservation().waitlistOfferExpiresAt() == Instant.parse("2026-05-09T10:00:00Z")
    }

    def "waitlist confirmation link confirms the offered reservation"() {
        given: "A waitlisted participant has received an offer"
            def term = createTerm(1)
            def confirmed = reserve(term, "Anna", "anna@example.com").booking().reservation()
            reserve(term, "Jan", "jan@example.com")
            cancelReservation.cancel(confirmed.cancellationToken())
            def token = notifications.waitlistOffers().first().reservation().waitlistConfirmationToken()

        when: "The participant confirms the offer"
            def promoted = confirmWaitlistOffer.confirm(token)

        then: "The reservation becomes confirmed"
            promoted.status() == ReservationStatus.CONFIRMED
            promoted.waitlistConfirmationToken() == null
    }

    def "expired waitlist offer is closed and next waitlisted reservation is offered"() {
        given: "Two participants are waiting and the first receives an offer"
            def term = createTerm(1)
            def confirmed = reserve(term, "Anna", "anna@example.com").booking().reservation()
            reserve(term, "Jan", "jan@example.com")
            reserve(term, "Ewa", "ewa@example.com")
            cancelReservation.cancel(confirmed.cancellationToken())

        when: "The instructor expires stale offers after the link TTL"
            clock.instant = Instant.parse("2026-05-09T10:01:00Z")
            def expired = expireWaitlistOffers.expire(owner, term.id())

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
            def term = createTerm(1)
            def confirmed = reserve(term, "Anna", "anna@example.com").booking().reservation()
            clock.instant = Instant.parse("2026-05-31T00:00:01Z")

        when: "The participant tries to cancel after the deadline"
            cancelReservation.cancel(confirmed.cancellationToken())

        then: "The cancellation is rejected"
            thrown(TermValidationException)
    }

    private CreateReservationResult reserve(Term term, String firstName, String email, boolean createAccount = false) {
        createReservation.create(
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

    private Term createTerm(int capacity) {
        terms.save(Term.create(
                owner,
                "Basic pistol",
                "",
                new Location("Range A", "Range Street 1", 52.2297d, 21.0122d),
                capacity,
                1,
                60,
                LocalDateTime.parse("2026-06-01T12:00:00"),
                Instant.parse("2026-05-08T10:00:00Z")
        ))
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
