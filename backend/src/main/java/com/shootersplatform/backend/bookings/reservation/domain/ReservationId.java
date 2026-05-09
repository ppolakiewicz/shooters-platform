package com.shootersplatform.backend.bookings.reservation.domain;

import java.util.UUID;

public record ReservationId(UUID value) {

    public static ReservationId newId() {
        return new ReservationId(UUID.randomUUID());
    }
}
