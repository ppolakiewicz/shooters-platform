package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.term.domain.TermService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelReservationByParticipantUseCase {

    private final ReservationService reservations;
    private final TermService terms;
    private final WaitlistPromotionCoordinator waitlistPromotion;

    CancelReservationByParticipantUseCase(
            ReservationService reservations,
            TermService terms,
            WaitlistPromotionCoordinator waitlistPromotion
    ) {
        this.reservations = reservations;
        this.terms = terms;
        this.waitlistPromotion = waitlistPromotion;
    }

    @Transactional
    public Reservation cancel(String cancellationToken) {
        Reservation reservation = reservations.participantCancellation(cancellationToken);
        Term term = terms.requireParticipantCancellationAllowed(reservation.termId());
        Reservation cancelled = reservations.cancelByParticipant(reservation);
        waitlistPromotion.promoteIfPossible(term);
        return cancelled;
    }
}
