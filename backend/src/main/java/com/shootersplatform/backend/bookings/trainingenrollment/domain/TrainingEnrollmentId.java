package com.shootersplatform.backend.bookings.trainingenrollment.domain;

import java.util.UUID;

public record TrainingEnrollmentId(UUID value) {

    public static TrainingEnrollmentId newId() {
        return new TrainingEnrollmentId(UUID.randomUUID());
    }
}
