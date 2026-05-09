package com.shootersplatform.backend.bookings.location.domain;

import org.jspecify.annotations.Nullable;

public record Location(
        String placeName,
        String address,
        double latitude,
        double longitude
) {

    private static final int TEXT_MAX_LENGTH = 240;

    public Location {
        placeName = normalizeRequired(placeName, "Place name");
        address = normalizeRequired(address, "Address");
        if (latitude < -90 || latitude > 90) {
            throw new LocationValidationException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new LocationValidationException("Longitude must be between -180 and 180");
        }
    }

    private static String normalizeRequired(@Nullable String value, String fieldName) {
        if (value == null) {
            throw new LocationValidationException(fieldName + " is required");
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new LocationValidationException(fieldName + " is required");
        }
        if (normalized.length() > TEXT_MAX_LENGTH) {
            throw new LocationValidationException(fieldName + " cannot exceed " + TEXT_MAX_LENGTH + " characters");
        }
        return normalized;
    }
}
