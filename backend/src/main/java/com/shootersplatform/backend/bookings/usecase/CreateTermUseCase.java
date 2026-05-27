package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.location.domain.Location;
import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.term.domain.TermService;
import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CreateTermUseCase {

    private final TermService terms;

    CreateTermUseCase(TermService terms) {
        this.terms = terms;
    }

    @Transactional
    public AvailableTerm create(
            UserId ownerId,
            String name,
            String description,
            TrainingLevel trainingLevel,
            Location location,
            int capacity,
            int cancellationDeadlineDays,
            int durationMinutes,
            LocalDateTime startsAt
    ) {
        Term term = terms.create(ownerId, name, description, trainingLevel, location, capacity, cancellationDeadlineDays, durationMinutes, startsAt);
        return new AvailableTerm(term, term.capacity());
    }
}
