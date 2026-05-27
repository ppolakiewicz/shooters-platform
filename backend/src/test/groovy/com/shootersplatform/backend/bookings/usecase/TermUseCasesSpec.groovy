package com.shootersplatform.backend.bookings.usecase

import com.shootersplatform.backend.bookings.location.domain.Location
import com.shootersplatform.backend.bookings.term.domain.Term
import com.shootersplatform.backend.bookings.term.domain.TermNotFoundException
import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel
import com.shootersplatform.backend.identity.domain.UserId
import spock.lang.Specification

import java.time.LocalDateTime

class TermUseCasesSpec extends Specification {

    private UserId owner = UserId.newId()
    private UserId otherOwner = UserId.newId()
    private BookingUseCaseTestContext booking

    def setup() {
        booking = new BookingUseCaseTestContext()
    }

    def "creates term and exposes it on the owner list with full availability"() {
        when: "The instructor creates a term"
            def created = createTerm(4)

        then: "The term is returned and visible to its owner"
            created.availablePlaces() == 4
            created.term().trainingLevel() == TrainingLevel.BASIC
            booking.listOwnerTerms.list(owner)*.term()*.id() == [created.term().id()]
    }

    def "lists only future public terms and combines them with reservation availability"() {
        given: "Past and future public terms exist"
            createTerm(2, owner, "Past pistol", LocalDateTime.parse("2026-05-08T11:59:59"))
            def future = createTerm(2, owner, "Future pistol", LocalDateTime.parse("2026-05-08T12:00:01"))

        and: "One participant reserves the future term"
            reserve(future.term(), "Anna", "anna@example.com")

        when: "Public terms are listed"
            def publicTerms = booking.listPublicTerms.list()

        then: "Only the future term is returned with remaining places"
            publicTerms*.term()*.id() == [future.term().id()]
            publicTerms*.availablePlaces() == [1]
    }

    def "lists owner terms only for the requested owner and reports availability"() {
        given: "Two owners have terms"
            def ownerTerm = createTerm(2, owner, "Owner pistol")
            createTerm(2, otherOwner, "Other pistol")

        and: "The owner's term has an occupied place"
            reserve(ownerTerm.term(), "Anna", "anna@example.com")

        expect: "Only the requested owner's term is returned"
            booking.listOwnerTerms.list(owner)*.term()*.id() == [ownerTerm.term().id()]
            booking.listOwnerTerms.list(owner)*.availablePlaces() == [1]
    }

    def "gets public term with calculated availability"() {
        given: "A public term has one occupied place"
            def term = createTerm(3).term()
            reserve(term, "Anna", "anna@example.com")

        when: "The public term is read"
            def availableTerm = booking.getPublicTerm.get(term.id())

        then: "The use case reports remaining places"
            availableTerm.term().id() == term.id()
            availableTerm.availablePlaces() == 2
    }

    def "updates editable term fields while preserving capacity and availability"() {
        given: "A term with one reservation exists"
            def term = createTerm(3).term()
            reserve(term, "Anna", "anna@example.com")

        when: "The instructor updates editable fields"
            def updated = booking.updateTerm.update(
                    owner,
                    term.id(),
                    "Advanced pistol",
                    "Updated",
                    TrainingLevel.ADVANCED,
                    location(),
                    2,
                    90,
                    LocalDateTime.parse("2026-06-01T14:00:00")
            )

        then: "Capacity is preserved and availability is recalculated"
            updated.term().name() == "Advanced pistol"
            updated.term().trainingLevel() == TrainingLevel.ADVANCED
            updated.term().capacity() == 3
            updated.term().cancellationDeadlineDays() == 2
            updated.term().durationMinutes() == 90
            updated.availablePlaces() == 2
    }

    def "rejects update by a different owner"() {
        given: "A term exists"
            def term = createTerm(2).term()

        when: "Another owner tries to update it"
            booking.updateTerm.update(otherOwner, term.id(), "Advanced pistol", "", TrainingLevel.ADVANCED, location(), 1, 60, LocalDateTime.parse("2026-06-01T14:00:00"))

        then: "The term is hidden from the other owner"
            thrown(TermNotFoundException)
    }

    private AvailableTerm createTerm(
            int capacity,
            UserId ownerId = owner,
            String name = "Basic pistol",
            LocalDateTime startsAt = LocalDateTime.parse("2026-06-01T12:00:00")
    ) {
        booking.createTerm.create(ownerId, name, "", TrainingLevel.BASIC, location(), capacity, 1, 60, startsAt)
    }

    private CreateReservationResult reserve(Term term, String firstName, String email) {
        booking.createReservation.create(term.id(), null, firstName, "Nowak", email, "+48111111111", false, null, null, "127.0.0.1")
    }

    private static Location location() {
        new Location("Range A", "Range Street 1", 52.2297d, 21.0122d)
    }
}
