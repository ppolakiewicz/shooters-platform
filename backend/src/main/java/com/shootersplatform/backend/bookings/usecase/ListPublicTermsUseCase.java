package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.term.domain.TermService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListPublicTermsUseCase {

    private final TermService terms;
    private final ReservationService reservations;

    ListPublicTermsUseCase(TermService terms, ReservationService reservations) {
        this.terms = terms;
        this.reservations = reservations;
    }

    @Transactional(readOnly = true)
    public List<AvailableTerm> list() {
        return terms.listPublic().stream().map(this::withAvailability).toList();
    }

    private AvailableTerm withAvailability(Term term) {
        long occupiedPlaces = reservations.countOccupiedPlaces(term.id());
        return new AvailableTerm(term, Math.max(0, term.capacity() - Math.toIntExact(occupiedPlaces)));
    }
}
