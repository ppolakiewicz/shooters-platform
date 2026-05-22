package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelReservationByParticipantUseCase {

    private final ReservationService reservations;

    public CancelReservationByParticipantUseCase(ReservationService reservations) {
        this.reservations = reservations;
    }

    @Transactional
    public Reservation cancel(String cancellationToken) {
        return reservations.cancelByParticipant(cancellationToken);
    }
}
