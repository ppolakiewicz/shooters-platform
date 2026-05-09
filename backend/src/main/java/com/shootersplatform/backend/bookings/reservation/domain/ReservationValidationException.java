package com.shootersplatform.backend.bookings.reservation.domain;

public class ReservationValidationException extends RuntimeException {

    public ReservationValidationException(String message) {
        super(message);
    }
}
