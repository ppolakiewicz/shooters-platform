package com.shootersplatform.backend.bookings.term.usecase;

import com.shootersplatform.backend.bookings.location.domain.Location;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.term.domain.TermService;
import com.shootersplatform.backend.bookings.term.domain.TermValidationException;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UpdateTermUseCase {

    private final TermService terms;
    private final ReservationService reservations;

    public UpdateTermUseCase(TermService terms, ReservationService reservations) {
        this.terms = terms;
        this.reservations = reservations;
    }

    @Transactional
    public Term update(
            UserId ownerId,
            TermId termId,
            String name,
            String description,
            Location location,
            int capacity,
            int cancellationDeadlineDays,
            int durationMinutes,
            LocalDateTime startsAt
    ) {
        long occupiedPlaces = reservations.countOccupiedPlaces(termId);
        if (capacity < occupiedPlaces) {
            throw new TermValidationException("Capacity cannot be lower than occupied places");
        }
        return terms.update(ownerId, termId, name, description, location, capacity, cancellationDeadlineDays, durationMinutes, startsAt);
    }
}
