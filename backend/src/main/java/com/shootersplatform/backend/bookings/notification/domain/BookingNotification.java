package com.shootersplatform.backend.bookings.notification.domain;

import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.bookings.term.domain.Term;

public record BookingNotification(
        BookingNotificationType type,
        Term term,
        Reservation reservation
) {

    public static BookingNotification reservationConfirmed(Term term, Reservation reservation) {
        return new BookingNotification(BookingNotificationType.RESERVATION_CONFIRMED, term, reservation);
    }

    public static BookingNotification waitlistOfferCreated(Term term, Reservation reservation) {
        return new BookingNotification(BookingNotificationType.WAITLIST_OFFER_CREATED, term, reservation);
    }
}
