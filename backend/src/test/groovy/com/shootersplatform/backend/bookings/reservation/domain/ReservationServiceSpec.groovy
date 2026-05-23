package com.shootersplatform.backend.bookings.reservation.domain

import com.shootersplatform.backend.bookings.term.domain.TermId
import com.shootersplatform.backend.identity.domain.EmailAddress
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class ReservationServiceSpec extends Specification {

    private InMemoryReservationRepository reservations
    private ReservationService service
    private MutableClock clock

    def setup() {
        reservations = new InMemoryReservationRepository()
        clock = new MutableClock(Instant.parse("2026-05-08T10:00:00Z"))
        service = new ReservationService(reservations, clock)
    }

    def "creates confirmed reservation"() {
        given: "A term id exists"
            def termId = TermId.newId()

        when: "A reservation is created"
            def reservation = service.createConfirmed(termId, null, "Anna", "Nowak", "anna@example.com", "+48111111111")

        then: "The reservation is confirmed and stored"
            reservation.status() == ReservationStatus.CONFIRMED
            reservation.termId() == termId
            reservations.findByTerm(termId) == [reservation]
    }

    def "reports active reservation email for duplicate checks"() {
        given: "A participant has an active reservation"
            def termId = TermId.newId()
            service.createConfirmed(termId, null, "Anna", "Nowak", "anna@example.com", "+48111111111")

        expect: "The normalized email is treated as active"
            service.hasActiveReservation(termId, new EmailAddress("ANNA@example.com"))
    }

    def "participant cancellation closes an active reservation"() {
        given: "A confirmed reservation exists"
            def reservation = service.createConfirmed(TermId.newId(), null, "Anna", "Nowak", "anna@example.com", "+48111111111")

        when: "The reservation is cancelled"
            def cancelled = service.cancelByParticipant(reservation)

        then: "The reservation is closed"
            cancelled.status() == ReservationStatus.CANCELLED_BY_PARTICIPANT
            !cancelled.activeForDuplicateCheck()
    }

    def "waitlist confirmation link confirms the offered reservation"() {
        given: "A waitlist offer exists"
            def offered = service.createWaitlistOffer(
                    TermId.newId(),
                    null,
                    "Jan",
                    "Nowak",
                    new EmailAddress("jan@example.com"),
                    "+48222222222",
                    "cancel-token",
                    Instant.parse("2026-05-09T10:00:00Z")
            )

        when: "The participant confirms the offer"
            def confirmed = service.confirmWaitlistOffer(offered, offered.waitlistConfirmationToken())

        then: "The reservation becomes confirmed"
            confirmed.status() == ReservationStatus.CONFIRMED
            confirmed.waitlistConfirmationToken() == null
            confirmed.waitlistOfferExpiresAt() == null
    }

    def "expired waitlist offer is closed for a term"() {
        given: "A waitlist offer is stale"
            def termId = TermId.newId()
            service.createWaitlistOffer(
                    termId,
                    null,
                    "Jan",
                    "Nowak",
                    new EmailAddress("jan@example.com"),
                    "+48222222222",
                    "cancel-token",
                    Instant.parse("2026-05-09T10:00:00Z")
            )
            clock.instant = Instant.parse("2026-05-09T10:01:00Z")

        when: "Expired offers are closed"
            def expired = service.expireWaitlistOffers(termId)

        then: "Only the stale offer is expired"
            expired == 1
            reservations.findByTerm(termId).first().status() == ReservationStatus.WAITLIST_OFFER_EXPIRED
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
