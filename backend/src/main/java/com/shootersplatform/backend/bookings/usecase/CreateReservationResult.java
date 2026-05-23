package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import org.jspecify.annotations.Nullable;

public record CreateReservationResult(
        CreatedBooking booking,
        @Nullable AuthenticatedUser registeredUser
) {
}
