package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.term.domain.TermService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetPublicTermUseCase {

    private final TermService terms;
    private final ReservationService reservations;

    GetPublicTermUseCase(TermService terms, ReservationService reservations) {
        this.terms = terms;
        this.reservations = reservations;
    }

    @Transactional(readOnly = true)
    public AvailableTerm get(TermId termId) {
        Term term = terms.getPublic(termId);
        long occupiedPlaces = reservations.countOccupiedPlaces(term.id());
        return new AvailableTerm(term, Math.max(0, term.capacity() - Math.toIntExact(occupiedPlaces)));
    }
}
