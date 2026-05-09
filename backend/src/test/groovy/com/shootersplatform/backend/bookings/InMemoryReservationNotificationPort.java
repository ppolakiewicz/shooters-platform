package com.shootersplatform.backend.bookings;

import com.shootersplatform.backend.bookings.reservation.domain.ReservationNotificationPort;
import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.bookings.term.domain.Term;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class InMemoryReservationNotificationPort implements ReservationNotificationPort {

    private final List<Reservation> confirmed = new ArrayList<>();
    private final List<Reservation> waitlistOffers = new ArrayList<>();

    @Override
    public void reservationConfirmed(Term term, Reservation reservation) {
        confirmed.add(reservation);
    }

    @Override
    public void waitlistOfferCreated(Term term, Reservation reservation) {
        waitlistOffers.add(reservation);
    }

    public List<Reservation> confirmed() {
        return List.copyOf(confirmed);
    }

    public List<Reservation> waitlistOffers() {
        return List.copyOf(waitlistOffers);
    }
}
