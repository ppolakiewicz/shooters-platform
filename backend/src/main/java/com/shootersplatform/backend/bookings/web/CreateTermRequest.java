package com.shootersplatform.backend.bookings.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

record CreateTermRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 2048) String description,
        @Valid @NotNull LocationRequest location,
        @Min(1) @Max(1000) int capacity,
        @Min(0) @Max(365) int cancellationDeadlineDays,
        @Min(1) @Max(1440) int durationMinutes,
        @NotNull LocalDateTime startsAt
) {
}
