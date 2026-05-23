package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.term.domain.TermService;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListReservationsUseCase {

    private final ReservationService reservations;
    private final TermService terms;

    ListReservationsUseCase(ReservationService reservations, TermService terms) {
        this.reservations = reservations;
        this.terms = terms;
    }

    @Transactional(readOnly = true)
    public List<Reservation> list(UserId ownerId, TermId termId) {
        terms.requireOwned(ownerId, termId);
        return reservations.listReservations(termId);
    }
}
