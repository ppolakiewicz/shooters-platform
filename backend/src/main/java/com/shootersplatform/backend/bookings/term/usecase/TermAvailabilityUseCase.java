package com.shootersplatform.backend.bookings.term.usecase;

import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.term.domain.TermService;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TermAvailabilityUseCase {

    private final TermService terms;
    private final ReservationService reservations;

    public TermAvailabilityUseCase(TermService terms, ReservationService reservations) {
        this.terms = terms;
        this.reservations = reservations;
    }

    @Transactional(readOnly = true)
    public List<AvailableTerm> listPublic() {
        return terms.listPublic().stream().map(this::withAvailability).toList();
    }

    @Transactional(readOnly = true)
    public AvailableTerm getPublic(TermId termId) {
        return withAvailability(terms.getPublic(termId));
    }

    @Transactional(readOnly = true)
    public List<AvailableTerm> listOwner(UserId ownerId) {
        return terms.listOwner(ownerId).stream().map(this::withAvailability).toList();
    }

    @Transactional(readOnly = true)
    public AvailableTerm withAvailability(Term term) {
        long occupiedPlaces = reservations.countOccupiedPlaces(term.id());
        return new AvailableTerm(term, Math.max(0, term.capacity() - Math.toIntExact(occupiedPlaces)));
    }
}
