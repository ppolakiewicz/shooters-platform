package com.shootersplatform.backend.bookings.term.domain;

import com.shootersplatform.backend.bookings.location.domain.Location;
import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record Term(
        TermId id,
        UserId ownerId,
        String name,
        String description,
        Location location,
        int capacity,
        int cancellationDeadlineDays,
        int durationMinutes,
        LocalDateTime startsAt,
        Instant createdAt,
        Instant updatedAt
) {

    private static final ZoneId BOOKING_ZONE = ZoneId.of("Europe/Warsaw");
    private static final int NAME_MAX_LENGTH = 120;
    private static final int DESCRIPTION_MAX_LENGTH = 2048;
    private static final int CAPACITY_MIN = 1;
    private static final int CANCELLATION_DEADLINE_DAYS_MIN = 0;
    private static final int DURATION_MINUTES_MIN = 1;

    public static Term create(
            UserId ownerId,
            String name,
            String description,
            Location location,
            int capacity,
            int cancellationDeadlineDays,
            int durationMinutes,
            LocalDateTime startsAt,
            Instant now
    ) {
        return new Term(
                TermId.newId(),
                ownerId,
                name,
                description,
                location,
                capacity,
                cancellationDeadlineDays,
                durationMinutes,
                startsAt,
                now,
                now
        );
    }

    public Term {
        name = normalizeName(name);
        description = normalizeDescription(description);
        if (capacity < CAPACITY_MIN) {
            throw new TermValidationException("Capacity must be at least " + CAPACITY_MIN);
        }
        if (cancellationDeadlineDays < CANCELLATION_DEADLINE_DAYS_MIN) {
            throw new TermValidationException("Cancellation deadline days cannot be negative");
        }
        if (durationMinutes < DURATION_MINUTES_MIN) {
            throw new TermValidationException("Duration must be at least " + DURATION_MINUTES_MIN + " minute");
        }
    }

    public Term update(
            String updatedName,
            String updatedDescription,
            Location updatedLocation,
            int updatedCancellationDeadlineDays,
            int updatedDurationMinutes,
            LocalDateTime updatedStartsAt,
            Instant now
    ) {
        return new Term(id, ownerId, updatedName, updatedDescription, updatedLocation, capacity, updatedCancellationDeadlineDays, updatedDurationMinutes, updatedStartsAt, createdAt, now);
    }

    public boolean canParticipantCancel(Instant now) {
        LocalDate deadlineDate = startsAt.toLocalDate().minusDays(cancellationDeadlineDays);
        return !LocalDateTime.of(deadlineDate, java.time.LocalTime.MIN).atZone(BOOKING_ZONE).toInstant().isBefore(now);
    }

    public boolean startsInFuture(Instant now) {
        return startsAt.atZone(BOOKING_ZONE).toInstant().isAfter(now);
    }

    private static String normalizeName(@Nullable String value) {
        if (value == null) {
            throw new TermValidationException("Term name is required");
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new TermValidationException("Term name is required");
        }
        if (normalized.length() > NAME_MAX_LENGTH) {
            throw new TermValidationException("Term name cannot exceed " + NAME_MAX_LENGTH + " characters");
        }
        return normalized;
    }

    private static String normalizeDescription(@Nullable String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        if (normalized.length() > DESCRIPTION_MAX_LENGTH) {
            throw new TermValidationException("Term description cannot exceed " + DESCRIPTION_MAX_LENGTH + " characters");
        }
        return normalized;
    }
}
