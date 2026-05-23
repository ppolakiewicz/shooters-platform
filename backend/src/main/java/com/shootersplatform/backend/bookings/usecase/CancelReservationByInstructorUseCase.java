package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationId;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.term.domain.TermService;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelReservationByInstructorUseCase {

    private final ReservationService reservations;
    private final TermService terms;
    private final WaitlistPromotionCoordinator waitlistPromotion;

    CancelReservationByInstructorUseCase(
            ReservationService reservations,
            TermService terms,
            WaitlistPromotionCoordinator waitlistPromotion
    ) {
        this.reservations = reservations;
        this.terms = terms;
        this.waitlistPromotion = waitlistPromotion;
    }

    @Transactional
    public Reservation cancel(UserId ownerId, TermId termId, ReservationId reservationId) {
        Term term = terms.requireOwnedForUpdate(ownerId, termId);
        Reservation cancelled = reservations.cancelByInstructor(term.id(), reservationId);
        waitlistPromotion.promoteIfPossible(term);
        return cancelled;
    }
}
