package com.shootersplatform.backend.bookings.waitlist.domain;

public class WaitlistNotFoundException extends RuntimeException {

    public WaitlistNotFoundException() {
        super("Waitlist entry was not found");
    }
}
