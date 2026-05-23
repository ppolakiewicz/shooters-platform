package com.shootersplatform.backend.bookings.waitlist

import com.shootersplatform.backend.bookings.waitlist.domain.InMemoryWaitlistRepository
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistService

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class WaitlistServiceTestConfiguration {

    private static final Instant BASE_TIME = Instant.parse("2026-05-08T10:00:00Z")

    private WaitlistServiceTestConfiguration() {
    }

    static WaitlistService inMemory() {
        return inMemory(Clock.fixed(BASE_TIME, ZoneOffset.UTC))
    }

    static WaitlistService inMemory(Clock clock) {
        return new WaitlistService(
                new InMemoryWaitlistRepository(),
                clock
        )
    }
}
