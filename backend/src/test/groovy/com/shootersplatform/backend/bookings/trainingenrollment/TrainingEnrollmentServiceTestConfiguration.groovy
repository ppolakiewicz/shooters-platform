package com.shootersplatform.backend.bookings.trainingenrollment

import com.shootersplatform.backend.bookings.trainingenrollment.domain.InMemoryTrainingEnrollmentRepository
import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollmentService

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class TrainingEnrollmentServiceTestConfiguration {

    private static final Instant BASE_TIME = Instant.parse("2026-05-08T10:00:00Z")

    private TrainingEnrollmentServiceTestConfiguration() {
    }

    static TrainingEnrollmentService inMemory() {
        return inMemory(Clock.fixed(BASE_TIME, ZoneOffset.UTC))
    }

    static TrainingEnrollmentService inMemory(Clock clock) {
        return new TrainingEnrollmentService(
                new InMemoryTrainingEnrollmentRepository(),
                clock
        )
    }
}
