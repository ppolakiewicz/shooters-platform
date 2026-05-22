package com.shootersplatform.backend.bookings.web;

import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.usecase.AvailableTerm;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

record TermResponse(
        UUID id,
        String name,
        String description,
        LocationResponse location,
        int capacity,
        int availablePlaces,
        int cancellationDeadlineDays,
        int durationMinutes,
        LocalDateTime startsAt,
        Instant createdAt,
        Instant updatedAt
) {

    static TermResponse from(Term term, int availablePlaces) {
        return new TermResponse(
                term.id().value(),
                term.name(),
                term.description(),
                LocationResponse.from(term.location()),
                term.capacity(),
                availablePlaces,
                term.cancellationDeadlineDays(),
                term.durationMinutes(),
                term.startsAt(),
                term.createdAt(),
                term.updatedAt()
        );
    }

    static TermResponse from(AvailableTerm availableTerm) {
        return from(availableTerm.term(), availableTerm.availablePlaces());
    }
}
