package com.shootersplatform.backend.bookings.term.web;

import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.location.web.LocationResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

record TermResponse(
        UUID id,
        String name,
        String description,
        LocationResponse location,
        int capacity,
        int cancellationDeadlineDays,
        int durationMinutes,
        LocalDateTime startsAt,
        Instant createdAt,
        Instant updatedAt
) {

    static TermResponse from(Term term) {
        return new TermResponse(
                term.id().value(),
                term.name(),
                term.description(),
                LocationResponse.from(term.location()),
                term.capacity(),
                term.cancellationDeadlineDays(),
                term.durationMinutes(),
                term.startsAt(),
                term.createdAt(),
                term.updatedAt()
        );
    }
}
