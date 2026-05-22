package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmWaitlistOfferUseCase {

    private final ReservationService reservations;

    public ConfirmWaitlistOfferUseCase(ReservationService reservations) {
        this.reservations = reservations;
    }

    @Transactional
    public Reservation confirm(String token) {
        return reservations.confirmWaitlistOffer(token);
    }
}
