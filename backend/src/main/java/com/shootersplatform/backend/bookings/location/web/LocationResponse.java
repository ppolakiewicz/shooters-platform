package com.shootersplatform.backend.bookings.location.web;

import com.shootersplatform.backend.bookings.location.domain.Location;

public record LocationResponse(
        String placeName,
        String address,
        double latitude,
        double longitude
) {

    public static LocationResponse from(Location location) {
        return new LocationResponse(location.placeName(), location.address(), location.latitude(), location.longitude());
    }
}
