package com.shootersplatform.backend.bookings.web;

import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel;
import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplate;

import java.time.Instant;
import java.util.UUID;

record TrainingTemplateResponse(
        UUID id,
        String name,
        String description,
        TrainingLevel trainingLevel,
        LocationResponse location,
        int capacity,
        int cancellationDeadlineDays,
        int durationMinutes,
        String defaultStartTime,
        Instant createdAt,
        Instant updatedAt
) {

    static TrainingTemplateResponse from(TrainingTemplate template) {
        return new TrainingTemplateResponse(
                template.id().value(),
                template.name(),
                template.description(),
                template.trainingLevel(),
                LocationResponse.from(template.location()),
                template.capacity(),
                template.cancellationDeadlineDays(),
                template.durationMinutes(),
                template.defaultStartTime().toString(),
                template.createdAt(),
                template.updatedAt()
        );
    }
}
