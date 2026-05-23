package com.shootersplatform.backend.bookings.term

import com.shootersplatform.backend.bookings.term.domain.InMemoryTermRepository
import com.shootersplatform.backend.bookings.term.domain.TermService

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class TermServiceTestConfiguration {

    private static final Instant BASE_TIME = Instant.parse("2026-05-08T10:00:00Z")

    private TermServiceTestConfiguration() {
    }

    static TermService inMemory() {
        return inMemory(Clock.fixed(BASE_TIME, ZoneOffset.UTC))
    }

    static TermService inMemory(Clock clock) {
        return new TermService(
                new InMemoryTermRepository(),
                clock
        )
    }
}
