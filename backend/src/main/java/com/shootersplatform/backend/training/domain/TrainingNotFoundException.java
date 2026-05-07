package com.shootersplatform.backend.training.domain;

public class TrainingNotFoundException extends RuntimeException {

    public TrainingNotFoundException() {
        super("Training was not found");
    }
}
