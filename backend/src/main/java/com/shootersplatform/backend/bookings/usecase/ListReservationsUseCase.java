package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListReservationsUseCase {

    private final ReservationService reservations;

    public ListReservationsUseCase(ReservationService reservations) {
        this.reservations = reservations;
    }

    @Transactional(readOnly = true)
    public List<Reservation> list(UserId ownerId, TermId termId) {
        return reservations.listReservations(ownerId, termId);
    }
}
