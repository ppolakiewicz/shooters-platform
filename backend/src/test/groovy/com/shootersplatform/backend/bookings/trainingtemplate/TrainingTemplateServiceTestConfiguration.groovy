package com.shootersplatform.backend.bookings.trainingtemplate

import com.shootersplatform.backend.bookings.trainingtemplate.domain.InMemoryTrainingTemplateRepository
import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplateService

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class TrainingTemplateServiceTestConfiguration {

    private static final Instant BASE_TIME = Instant.parse("2026-05-08T10:00:00Z")

    private TrainingTemplateServiceTestConfiguration() {
    }

    static TrainingTemplateService inMemory() {
        inMemory(Clock.fixed(BASE_TIME, ZoneOffset.UTC))
    }

    static TrainingTemplateService inMemory(Clock clock) {
        new TrainingTemplateService(new InMemoryTrainingTemplateRepository(), clock)
    }
}
