package com.shootersplatform.backend.bookings.web;

import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalTime;

record TrainingTemplateRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 2048) String description,
        @NotNull TrainingLevel trainingLevel,
        @Valid @NotNull LocationRequest location,
        @Min(1) @Max(10) int capacity,
        @Min(0) @Max(365) int cancellationDeadlineDays,
        @Min(30) @Max(1440) int durationMinutes,
        @NotNull LocalTime defaultStartTime
) {
}
