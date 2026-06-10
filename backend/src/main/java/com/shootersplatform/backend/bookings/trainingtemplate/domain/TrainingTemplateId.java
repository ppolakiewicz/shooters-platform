package com.shootersplatform.backend.bookings.trainingtemplate.domain;

import java.util.UUID;

public record TrainingTemplateId(UUID value) {

    public static TrainingTemplateId newId() {
        return new TrainingTemplateId(UUID.randomUUID());
    }
}
