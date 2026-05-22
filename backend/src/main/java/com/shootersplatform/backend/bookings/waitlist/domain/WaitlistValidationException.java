package com.shootersplatform.backend.bookings.waitlist.domain;

public class WaitlistValidationException extends RuntimeException {

    public WaitlistValidationException(String message) {
        super(message);
    }
}
