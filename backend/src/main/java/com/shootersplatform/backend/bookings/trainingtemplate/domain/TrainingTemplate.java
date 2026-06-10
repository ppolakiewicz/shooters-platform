package com.shootersplatform.backend.bookings.trainingtemplate.domain;

import com.shootersplatform.backend.bookings.location.domain.Location;
import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel;
import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Objects;

public record TrainingTemplate(
        TrainingTemplateId id,
        UserId ownerId,
        String name,
        String description,
        TrainingLevel trainingLevel,
        Location location,
        int capacity,
        int cancellationDeadlineDays,
        int durationMinutes,
        LocalTime defaultStartTime,
        Instant createdAt,
        Instant updatedAt
) {

    private static final int NAME_MAX_LENGTH = 120;
    private static final int DESCRIPTION_MAX_LENGTH = 2048;
    private static final int MIN_CAPACITY = 1;
    private static final int MAX_CAPACITY = 10;
    private static final int MIN_CANCELLATION_DEADLINE_DAYS = 0;
    private static final int MAX_CANCELLATION_DEADLINE_DAYS = 365;
    private static final int MIN_DURATION_MINUTES = 30;
    private static final int MAX_DURATION_MINUTES = 1440;
    private static final int DURATION_STEP_MINUTES = 30;
    private static final int START_TIME_STEP_MINUTES = 15;

    public static TrainingTemplate create(
            UserId ownerId,
            String name,
            String description,
            TrainingLevel trainingLevel,
            Location location,
            int capacity,
            int cancellationDeadlineDays,
            int durationMinutes,
            LocalTime defaultStartTime,
            Instant now
    ) {
        return new TrainingTemplate(
                TrainingTemplateId.newId(),
                ownerId,
                name,
                description,
                trainingLevel,
                location,
                capacity,
                cancellationDeadlineDays,
                durationMinutes,
                defaultStartTime,
                now,
                now
        );
    }

    public TrainingTemplate {
        Objects.requireNonNull(id, "Training template id is required");
        Objects.requireNonNull(ownerId, "Training template owner is required");
        name = normalizeName(name);
        description = normalizeDescription(description);
        Objects.requireNonNull(trainingLevel, "Training level is required");
        Objects.requireNonNull(location, "Location is required");
        validateNumbers(capacity, cancellationDeadlineDays, durationMinutes);
        validateDefaultStartTime(defaultStartTime);
        Objects.requireNonNull(createdAt, "Created timestamp is required");
        Objects.requireNonNull(updatedAt, "Updated timestamp is required");
    }

    public TrainingTemplate update(
            String updatedName,
            String updatedDescription,
            TrainingLevel updatedTrainingLevel,
            Location updatedLocation,
            int updatedCapacity,
            int updatedCancellationDeadlineDays,
            int updatedDurationMinutes,
            LocalTime updatedDefaultStartTime,
            Instant now
    ) {
        return new TrainingTemplate(
                id,
                ownerId,
                updatedName,
                updatedDescription,
                updatedTrainingLevel,
                updatedLocation,
                updatedCapacity,
                updatedCancellationDeadlineDays,
                updatedDurationMinutes,
                updatedDefaultStartTime,
                createdAt,
                now
        );
    }

    private static void validateNumbers(int capacity, int cancellationDeadlineDays, int durationMinutes) {
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            throw new TrainingTemplateValidationException(
                    "Capacity must be between " + MIN_CAPACITY + " and " + MAX_CAPACITY
            );
        }
        if (cancellationDeadlineDays < MIN_CANCELLATION_DEADLINE_DAYS
                || cancellationDeadlineDays > MAX_CANCELLATION_DEADLINE_DAYS) {
            throw new TrainingTemplateValidationException(
                    "Cancellation deadline days must be between "
                            + MIN_CANCELLATION_DEADLINE_DAYS + " and " + MAX_CANCELLATION_DEADLINE_DAYS
            );
        }
        if (durationMinutes < MIN_DURATION_MINUTES
                || durationMinutes > MAX_DURATION_MINUTES
                || durationMinutes % DURATION_STEP_MINUTES != 0) {
            throw new TrainingTemplateValidationException(
                    "Duration must be between " + MIN_DURATION_MINUTES + " and " + MAX_DURATION_MINUTES
                            + " minutes in " + DURATION_STEP_MINUTES + "-minute steps"
            );
        }
    }

    private static void validateDefaultStartTime(@Nullable LocalTime value) {
        if (value == null) {
            throw new TrainingTemplateValidationException("Default start time is required");
        }
        if (value.getSecond() != 0
                || value.getNano() != 0
                || value.getMinute() % START_TIME_STEP_MINUTES != 0) {
            throw new TrainingTemplateValidationException(
                    "Default start time must use " + START_TIME_STEP_MINUTES + "-minute steps without seconds"
            );
        }
    }

    private static String normalizeName(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new TrainingTemplateValidationException("Training template name is required");
        }
        String normalized = value.trim();
        if (normalized.length() > NAME_MAX_LENGTH) {
            throw new TrainingTemplateValidationException("Training template name cannot exceed " + NAME_MAX_LENGTH + " characters");
        }
        return normalized;
    }

    private static String normalizeDescription(@Nullable String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        if (normalized.length() > DESCRIPTION_MAX_LENGTH) {
            throw new TrainingTemplateValidationException("Training template description cannot exceed " + DESCRIPTION_MAX_LENGTH + " characters");
        }
        return normalized;
    }
}
