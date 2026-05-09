package com.shootersplatform.backend.bookings.location.web;

import com.shootersplatform.backend.bookings.location.domain.Location;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LocationRequest(
        @NotBlank @Size(max = 240) String placeName,
        @NotBlank @Size(max = 240) String address,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude
) {

    public Location toDomain() {
        return new Location(placeName, address, latitude, longitude);
    }
}
