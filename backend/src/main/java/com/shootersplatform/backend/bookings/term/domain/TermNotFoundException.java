package com.shootersplatform.backend.bookings.term.domain;

public class TermNotFoundException extends RuntimeException {

    public TermNotFoundException() {
        super("Term was not found");
    }
}
