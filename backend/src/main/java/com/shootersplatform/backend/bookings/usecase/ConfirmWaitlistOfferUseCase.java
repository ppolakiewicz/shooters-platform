package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationValidationException;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.term.domain.TermService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmWaitlistOfferUseCase {

    private final ReservationService reservations;
    private final TermService terms;

    ConfirmWaitlistOfferUseCase(ReservationService reservations, TermService terms) {
        this.reservations = reservations;
        this.terms = terms;
    }

    @Transactional
    public Reservation confirm(String token) {
        Reservation reservation = reservations.waitlistOffer(token);
        Term term = terms.requireForUpdate(reservation.termId());
        if (reservations.countOccupiedPlaces(term.id()) > term.capacity()) {
            throw new ReservationValidationException("Term capacity has already been reached");
        }
        return reservations.confirmWaitlistOffer(reservation, token);
    }
}
