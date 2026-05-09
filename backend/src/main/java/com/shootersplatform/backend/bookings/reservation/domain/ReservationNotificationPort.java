package com.shootersplatform.backend.bookings.reservation.domain;

import com.shootersplatform.backend.bookings.term.domain.Term;

public interface ReservationNotificationPort {

    void reservationConfirmed(Term term, Reservation reservation);

    void waitlistOfferCreated(Term term, Reservation reservation);
}
