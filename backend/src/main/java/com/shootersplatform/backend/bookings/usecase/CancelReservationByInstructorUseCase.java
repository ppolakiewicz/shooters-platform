package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationId;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelReservationByInstructorUseCase {

    private final ReservationService reservations;

    public CancelReservationByInstructorUseCase(ReservationService reservations) {
        this.reservations = reservations;
    }

    @Transactional
    public Reservation cancel(UserId ownerId, TermId termId, ReservationId reservationId) {
        return reservations.cancelByInstructor(ownerId, termId, reservationId);
    }
}
