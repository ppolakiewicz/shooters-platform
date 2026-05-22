package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpireWaitlistOffersUseCase {

    private final ReservationService reservations;

    public ExpireWaitlistOffersUseCase(ReservationService reservations) {
        this.reservations = reservations;
    }

    @Transactional
    public int expire(UserId ownerId, TermId termId) {
        return reservations.expireWaitlistOffers(ownerId, termId);
    }
}
