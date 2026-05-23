package com.shootersplatform.backend.bookings.term.domain


import com.shootersplatform.backend.bookings.location.domain.Location
import com.shootersplatform.backend.identity.domain.UserId
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class TermServiceSpec extends Specification {

    private UserId owner = UserId.newId()
    private InMemoryTermRepository terms
    private TermService service

    def setup() {
        terms = new InMemoryTermRepository()
        service = new TermService(terms, Clock.fixed(Instant.parse("2026-05-08T10:00:00Z"), ZoneOffset.UTC))
    }

    def "creates term from provided draft snapshot"() {
        when: "The instructor creates a concrete term from a draft"
            def term = createTerm()

        then: "The term stores copied editable fields"
            term.name() == "Basic pistol"
            term.description() == "Safety"
            term.location().address() == "Range Street 1"
            term.capacity() == 8
            term.cancellationDeadlineDays() == 2
            term.durationMinutes() == 90
            term.ownerId() == owner
    }

    def "preserves term capacity during update"() {
        given: "A term exists"
            def term = createTerm()

        when: "The instructor updates editable fields"
            def updated = service.update(owner, term.id(), "Updated pistol", "", location(), 3, 120, LocalDateTime.parse("2026-06-01T14:30:00"))

        then: "Capacity stays at the value chosen during creation"
            updated.name() == "Updated pistol"
            updated.capacity() == 8
            updated.cancellationDeadlineDays() == 3
            updated.durationMinutes() == 120
    }

    def "lists only public terms that start in the future"() {
        given: "Past and future terms exist"
            service.create(owner, "Past pistol", "", location(), 8, 2, 90, LocalDateTime.parse("2026-05-08T11:59:59"))
            def future = service.create(owner, "Future pistol", "", location(), 8, 2, 90, LocalDateTime.parse("2026-05-08T12:00:01"))

        when: "Public terms are listed"
            def publicTerms = service.listPublic()

        then: "Only the future term is returned"
            publicTerms*.id() == [future.id()]
    }

    def "requires reservable term that starts in the future"() {
        given: "A future term exists"
            def future = createTerm()

        expect: "The term can be reserved"
            service.requireReservable(future.id()) == future
    }

    def "rejects reservable term that has already started"() {
        given: "A past term exists"
            def past = service.create(owner, "Past pistol", "", location(), 8, 2, 90, LocalDateTime.parse("2026-05-08T11:59:59"))

        when: "The term is required for reservation"
            service.requireReservable(past.id())

        then: "The domain rejects late reservation"
            thrown(TermValidationException)
    }

    def "requires owned term for update"() {
        given: "A term belongs to the owner"
            def term = createTerm()

        expect: "The owner can lock the term"
            service.requireOwnedForUpdate(owner, term.id()) == term
    }

    def "rejects participant cancellation after configured deadline"() {
        given: "A term starts too soon to cancel"
            def term = service.create(owner, "Soon pistol", "", location(), 8, 1, 90, LocalDateTime.parse("2026-05-09T12:00:00"))

        when: "Cancellation is checked after Warsaw midnight deadline"
            service.requireParticipantCancellationAllowed(term.id())

        then: "The domain rejects cancellation"
            thrown(TermValidationException)
    }

    private static Location location() {
        new Location("Range A", "Range Street 1", 52.2297d, 21.0122d)
    }

    private Term createTerm() {
        service.create(owner, "Basic pistol", "Safety", location(), 8, 2, 90, LocalDateTime.parse("2026-06-01T12:30:00"))
    }
}
