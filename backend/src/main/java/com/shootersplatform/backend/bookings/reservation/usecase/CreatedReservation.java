package com.shootersplatform.backend.bookings.reservation.usecase;

import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import org.jspecify.annotations.Nullable;

public record CreatedReservation(
        Reservation reservation,
        @Nullable AuthenticatedUser registeredUser
) {
}
