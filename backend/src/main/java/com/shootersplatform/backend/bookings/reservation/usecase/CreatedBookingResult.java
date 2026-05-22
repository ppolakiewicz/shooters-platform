package com.shootersplatform.backend.bookings.reservation.usecase;

import com.shootersplatform.backend.bookings.reservation.domain.CreatedBooking;
import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import org.jspecify.annotations.Nullable;

public record CreatedBookingResult(
        CreatedBooking booking,
        @Nullable AuthenticatedUser registeredUser
) {
}
