package com.shootersplatform.backend.bookings.reservation.domain;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException() {
        super("Booking resource was not found");
    }
}
