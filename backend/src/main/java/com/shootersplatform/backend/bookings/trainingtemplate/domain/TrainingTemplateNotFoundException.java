package com.shootersplatform.backend.bookings.trainingtemplate.domain;

public class TrainingTemplateNotFoundException extends RuntimeException {

    public TrainingTemplateNotFoundException() {
        super("Training template was not found");
    }
}
