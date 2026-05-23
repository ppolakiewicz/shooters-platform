package com.shootersplatform.backend.bookings.waitlist.domain

import com.shootersplatform.backend.bookings.term.domain.TermId
import com.shootersplatform.backend.identity.domain.EmailAddress
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class WaitlistServiceSpec extends Specification {

    private InMemoryWaitlistRepository waitlist
    private WaitlistService service

    def setup() {
        waitlist = new InMemoryWaitlistRepository()
        service = new WaitlistService(waitlist, Clock.fixed(Instant.parse("2026-05-08T10:00:00Z"), ZoneOffset.UTC))
    }

    def "adds waitlist entries with sequential positions"() {
        given: "A term exists"
            def termId = TermId.newId()

        when: "Two participants join the waitlist"
            def first = service.add(termId, null, "Anna", "Nowak", "anna@example.com", "+48111111111")
            def second = service.add(termId, null, "Jan", "Nowak", "jan@example.com", "+48222222222")

        then: "Entries keep queue order"
            first.position() == 1
            second.position() == 2
            service.listEntries(termId)*.email()*.value() == ["anna@example.com", "jan@example.com"]
    }

    def "polls first entry and compacts remaining positions"() {
        given: "A term has a waitlist"
            def termId = TermId.newId()
            service.add(termId, null, "Anna", "Nowak", "anna@example.com", "+48111111111")
            service.add(termId, null, "Jan", "Nowak", "jan@example.com", "+48222222222")

        when: "The first entry is taken"
            def first = service.pollFirst(termId)

        then: "The next entry moves to the first position"
            first.present
            first.get().email().value() == "anna@example.com"
            service.listEntries(termId)*.position() == [1]
            service.listEntries(termId)*.email()*.value() == ["jan@example.com"]
    }

    def "reports participant already on waitlist"() {
        given: "A participant is waitlisted"
            def termId = TermId.newId()
            service.add(termId, null, "Anna", "Nowak", "anna@example.com", "+48111111111")

        expect: "The normalized email is found"
            service.hasParticipant(termId, new EmailAddress("ANNA@example.com"))
    }
}
