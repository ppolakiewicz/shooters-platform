package com.shootersplatform.backend.bookings.location.domain;

public class LocationValidationException extends RuntimeException {

    public LocationValidationException(String message) {
        super(message);
    }
}
