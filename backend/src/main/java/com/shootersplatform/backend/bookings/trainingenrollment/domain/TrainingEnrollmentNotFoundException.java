package com.shootersplatform.backend.bookings.trainingenrollment.domain;

public class TrainingEnrollmentNotFoundException extends RuntimeException {

    public TrainingEnrollmentNotFoundException() {
        super("Training enrollment was not found");
    }
}
