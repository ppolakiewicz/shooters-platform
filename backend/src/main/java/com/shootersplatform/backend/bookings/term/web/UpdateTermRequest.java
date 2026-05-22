package com.shootersplatform.backend.bookings.term.web;

import com.shootersplatform.backend.bookings.location.web.LocationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

record UpdateTermRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 2048) String description,
        @Valid @NotNull LocationRequest location,
        @Nullable @Min(1) @Max(1000) Integer capacity,
        @Min(0) @Max(365) int cancellationDeadlineDays,
        @Min(1) @Max(1440) int durationMinutes,
        @NotNull LocalDateTime startsAt
) {
}
