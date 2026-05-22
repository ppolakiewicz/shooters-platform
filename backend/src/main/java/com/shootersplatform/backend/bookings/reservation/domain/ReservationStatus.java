package com.shootersplatform.backend.bookings.reservation.domain;

public enum ReservationStatus {
    CONFIRMED,
    WAITLIST_OFFERED,
    CANCELLED_BY_PARTICIPANT,
    CANCELLED_BY_INSTRUCTOR,
    WAITLIST_OFFER_EXPIRED
}
