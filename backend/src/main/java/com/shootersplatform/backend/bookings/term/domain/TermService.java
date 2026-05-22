package com.shootersplatform.backend.bookings.term.domain;

import com.shootersplatform.backend.bookings.location.domain.Location;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TermService {

    private final TermRepository terms;
    private final Clock clock;

    public TermService(TermRepository terms, Clock clock) {
        this.terms = terms;
        this.clock = clock;
    }

    public Term create(
            UserId ownerId,
            String name,
            String description,
            Location location,
            int capacity,
            int cancellationDeadlineDays,
            int durationMinutes,
            LocalDateTime startsAt
    ) {
        return terms.save(Term.create(ownerId, name, description, location, capacity, cancellationDeadlineDays, durationMinutes, startsAt, clock.instant()));
    }

    public Term update(
            UserId ownerId,
            TermId termId,
            String name,
            String description,
            Location location,
            int cancellationDeadlineDays,
            int durationMinutes,
            LocalDateTime startsAt) {

        Term term = terms.findByIdAndOwner(termId, ownerId).orElseThrow(TermNotFoundException::new);
        return terms.save(term.update(name, description, location, cancellationDeadlineDays, durationMinutes, startsAt, clock.instant()));
    }

    @Transactional(readOnly = true)
    public List<Term> listPublic() {
        return terms.findPublicTerms().stream()
                .filter(term -> term.startsInFuture(clock.instant()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Term getPublic(TermId termId) {
        return terms.findById(termId).orElseThrow(TermNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<Term> listOwner(UserId ownerId) {
        return terms.findByOwner(ownerId);
    }
}
