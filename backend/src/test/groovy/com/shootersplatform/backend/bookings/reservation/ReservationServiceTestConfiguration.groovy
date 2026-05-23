package com.shootersplatform.backend.bookings.reservation

import com.shootersplatform.backend.bookings.reservation.domain.InMemoryReservationRepository
import com.shootersplatform.backend.bookings.reservation.domain.ReservationService

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class ReservationServiceTestConfiguration {

    private static final Instant BASE_TIME = Instant.parse("2026-05-08T10:00:00Z")

    private ReservationServiceTestConfiguration() {
    }

    static ReservationService inMemory() {
        return inMemory(Clock.fixed(BASE_TIME, ZoneOffset.UTC))
    }

    static ReservationService inMemory(Clock clock) {
        return new ReservationService(
                new InMemoryReservationRepository(),
                clock
        )
    }
}
