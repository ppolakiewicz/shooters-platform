package com.shootersplatform.backend.bookings.term.usecase;

import com.shootersplatform.backend.bookings.location.domain.Location;
import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.term.domain.TermService;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UpdateTermUseCase {

    private final TermService terms;

    public UpdateTermUseCase(TermService terms) {
        this.terms = terms;
    }

    @Transactional
    public Term update(
            UserId ownerId,
            TermId termId,
            String name,
            String description,
            Location location,
            int cancellationDeadlineDays,
            int durationMinutes,
            LocalDateTime startsAt
    ) {
        return terms.update(ownerId, termId, name, description, location, cancellationDeadlineDays, durationMinutes, startsAt);
    }
}
