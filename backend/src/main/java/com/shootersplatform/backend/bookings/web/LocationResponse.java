package com.shootersplatform.backend.bookings.web;

import com.shootersplatform.backend.bookings.location.domain.Location;

record LocationResponse(
        String placeName,
        String address,
        double latitude,
        double longitude
) {

    static LocationResponse from(Location location) {
        return new LocationResponse(location.placeName(), location.address(), location.latitude(), location.longitude());
    }
}
