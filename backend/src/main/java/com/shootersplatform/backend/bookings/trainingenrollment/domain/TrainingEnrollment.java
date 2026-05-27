package com.shootersplatform.backend.bookings.trainingenrollment.domain;

import com.shootersplatform.backend.bookings.location.domain.Location;
import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel;
import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

public record TrainingEnrollment(
        TrainingEnrollmentId id,
        UserId ownerId,
        String name,
        String description,
        TrainingLevel trainingLevel,
        Location location,
        int capacity,
        int cancellationDeadlineDays,
        int durationMinutes,
        Instant createdAt,
        Instant updatedAt
) {

    private static final int NAME_MAX_LENGTH = 120;
    private static final int DESCRIPTION_MAX_LENGTH = 2048;

    public static TrainingEnrollment create(
            UserId ownerId,
            String name,
            String description,
            TrainingLevel trainingLevel,
            Location location,
            int capacity,
            int cancellationDeadlineDays,
            int durationMinutes,
            Instant now
    ) {
        return new TrainingEnrollment(TrainingEnrollmentId.newId(), ownerId, name, description, trainingLevel, location, capacity, cancellationDeadlineDays, durationMinutes, now, now);
    }

    public TrainingEnrollment {
        name = normalizeName(name);
        description = normalizeDescription(description);
        Objects.requireNonNull(trainingLevel, "Training level is required");
        validateNumbers(capacity, cancellationDeadlineDays, durationMinutes);
    }

    public TrainingEnrollment update(
            String updatedName,
            String updatedDescription,
            TrainingLevel updatedTrainingLevel,
            Location updatedLocation,
            int updatedCapacity,
            int updatedCancellationDeadlineDays,
            int updatedDurationMinutes,
            Instant now
    ) {
        return new TrainingEnrollment(id, ownerId, updatedName, updatedDescription, updatedTrainingLevel, updatedLocation, updatedCapacity, updatedCancellationDeadlineDays, updatedDurationMinutes, createdAt, now);
    }

    private static void validateNumbers(int capacity, int cancellationDeadlineDays, int durationMinutes) {
        if (capacity < 1) {
            throw new TrainingEnrollmentValidationException("Capacity must be at least 1");
        }
        if (cancellationDeadlineDays < 0) {
            throw new TrainingEnrollmentValidationException("Cancellation deadline days cannot be negative");
        }
        if (durationMinutes < 1) {
            throw new TrainingEnrollmentValidationException("Duration must be at least 1 minute");
        }
    }

    private static String normalizeName(@Nullable String value) {
        if (value == null) {
            throw new TrainingEnrollmentValidationException("Training enrollment name is required");
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new TrainingEnrollmentValidationException("Training enrollment name is required");
        }
        if (normalized.length() > NAME_MAX_LENGTH) {
            throw new TrainingEnrollmentValidationException("Training enrollment name cannot exceed " + NAME_MAX_LENGTH + " characters");
        }
        return normalized;
    }

    private static String normalizeDescription(@Nullable String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        if (normalized.length() > DESCRIPTION_MAX_LENGTH) {
            throw new TrainingEnrollmentValidationException("Training enrollment description cannot exceed " + DESCRIPTION_MAX_LENGTH + " characters");
        }
        return normalized;
    }
}
