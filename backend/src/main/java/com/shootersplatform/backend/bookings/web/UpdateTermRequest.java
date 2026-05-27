package com.shootersplatform.backend.bookings.web;

import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

record UpdateTermRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 2048) String description,
        @NotNull TrainingLevel trainingLevel,
        @Valid @NotNull LocationRequest location,
        @Nullable @Min(1) @Max(1000) Integer capacity,
        @Min(0) @Max(365) int cancellationDeadlineDays,
        @Min(1) @Max(1440) int durationMinutes,
        @NotNull LocalDateTime startsAt
) {
}
