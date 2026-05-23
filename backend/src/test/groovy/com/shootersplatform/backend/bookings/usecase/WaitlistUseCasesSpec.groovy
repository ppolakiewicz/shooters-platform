package com.shootersplatform.backend.bookings.usecase

import com.shootersplatform.backend.bookings.location.domain.Location
import com.shootersplatform.backend.bookings.term.domain.Term
import com.shootersplatform.backend.bookings.term.domain.TermNotFoundException
import com.shootersplatform.backend.identity.domain.UserId
import spock.lang.Specification

import java.time.LocalDateTime

class WaitlistUseCasesSpec extends Specification {

    private UserId owner = UserId.newId()
    private UserId otherOwner = UserId.newId()
    private BookingUseCaseTestContext booking

    def setup() {
        booking = new BookingUseCaseTestContext()
    }

    def "lists waitlist entries for the term owner"() {
        given: "A full term has two waitlist entries"
            def term = fullTermWithTwoWaitlistEntries()

        expect: "The owner can inspect the queue"
            booking.listWaitlistEntries.list(owner, term.id())*.email()*.value() == ["jan@example.com", "ewa@example.com"]
            booking.listWaitlistEntries.list(owner, term.id())*.position() == [1, 2]
    }

    def "listing waitlist entries requires term ownership"() {
        given: "A full term has a waitlist entry"
            def term = fullTermWithTwoWaitlistEntries()

        when: "Another owner tries to list the queue"
            booking.listWaitlistEntries.list(otherOwner, term.id())

        then: "The term is hidden from the other owner"
            thrown(TermNotFoundException)
    }

    def "participant cancels waitlist entry and remaining entries are compacted"() {
        given: "A full term has two waitlist entries"
            def term = fullTermWithTwoWaitlistEntries()
            def jan = booking.listWaitlistEntries.list(owner, term.id()).first()

        when: "The first waitlisted participant cancels"
            def cancelled = booking.cancelWaitlistEntryByParticipant.cancel(jan.cancellationToken())

        then: "The entry is removed and the remaining position is compacted"
            cancelled.email().value() == "jan@example.com"
            booking.listWaitlistEntries.list(owner, term.id())*.email()*.value() == ["ewa@example.com"]
            booking.listWaitlistEntries.list(owner, term.id())*.position() == [1]
    }

    def "owner removes waitlist entry and remaining entries are compacted"() {
        given: "A full term has two waitlist entries"
            def term = fullTermWithTwoWaitlistEntries()
            def jan = booking.listWaitlistEntries.list(owner, term.id()).first()

        when: "The owner removes the first waitlist entry"
            def removed = booking.removeWaitlistEntryByOwner.remove(owner, term.id(), jan.id())

        then: "The entry is removed and the remaining position is compacted"
            removed.email().value() == "jan@example.com"
            booking.listWaitlistEntries.list(owner, term.id())*.email()*.value() == ["ewa@example.com"]
            booking.listWaitlistEntries.list(owner, term.id())*.position() == [1]
    }

    def "owner removal requires term ownership"() {
        given: "A full term has a waitlist entry"
            def term = fullTermWithTwoWaitlistEntries()
            def jan = booking.listWaitlistEntries.list(owner, term.id()).first()

        when: "Another owner tries to remove the entry"
            booking.removeWaitlistEntryByOwner.remove(otherOwner, term.id(), jan.id())

        then: "The term is hidden from the other owner"
            thrown(TermNotFoundException)
    }

    private Term fullTermWithTwoWaitlistEntries() {
        def term = booking.createTerm.create(owner, "Basic pistol", "", location(), 1, 1, 60, LocalDateTime.parse("2026-06-01T12:00:00")).term()
        reserve(term, "Anna", "anna@example.com")
        reserve(term, "Jan", "jan@example.com")
        reserve(term, "Ewa", "ewa@example.com")
        return term
    }

    private CreateReservationResult reserve(Term term, String firstName, String email) {
        booking.createReservation.create(term.id(), null, firstName, "Nowak", email, "+48111111111", false, null, null, "127.0.0.1")
    }

    private static Location location() {
        new Location("Range A", "Range Street 1", 52.2297d, 21.0122d)
    }
}
