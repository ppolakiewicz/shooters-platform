package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.term.domain.TermService;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpireWaitlistOffersUseCase {

    private final ReservationService reservations;
    private final TermService terms;
    private final WaitlistPromotionCoordinator waitlistPromotion;

    ExpireWaitlistOffersUseCase(
            ReservationService reservations,
            TermService terms,
            WaitlistPromotionCoordinator waitlistPromotion
    ) {
        this.reservations = reservations;
        this.terms = terms;
        this.waitlistPromotion = waitlistPromotion;
    }

    @Transactional
    public int expire(UserId ownerId, TermId termId) {
        Term term = terms.requireOwnedForUpdate(ownerId, termId);
        int expired = reservations.expireWaitlistOffers(term.id());
        if (expired > 0) {
            waitlistPromotion.promoteIfPossible(term);
        }
        return expired;
    }
}
