package com.shootersplatform.backend.bookings.trainingenrollment.web;

import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollment;
import com.shootersplatform.backend.bookings.location.web.LocationResponse;

import java.time.Instant;
import java.util.UUID;

record TrainingEnrollmentResponse(
        UUID id,
        String name,
        String description,
        LocationResponse location,
        int capacity,
        int cancellationDeadlineDays,
        int durationMinutes,
        Instant createdAt,
        Instant updatedAt
) {

    static TrainingEnrollmentResponse from(TrainingEnrollment enrollment) {
        return new TrainingEnrollmentResponse(
                enrollment.id().value(),
                enrollment.name(),
                enrollment.description(),
                LocationResponse.from(enrollment.location()),
                enrollment.capacity(),
                enrollment.cancellationDeadlineDays(),
                enrollment.durationMinutes(),
                enrollment.createdAt(),
                enrollment.updatedAt()
        );
    }
}
