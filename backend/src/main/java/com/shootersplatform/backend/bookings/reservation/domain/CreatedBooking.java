package com.shootersplatform.backend.bookings.reservation.domain;

import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistEntry;
import org.jspecify.annotations.Nullable;

public record CreatedBooking(
        @Nullable Reservation reservation,
        @Nullable WaitlistEntry waitlistEntry
) {

    public static CreatedBooking reservation(Reservation reservation) {
        return new CreatedBooking(reservation, null);
    }

    public static CreatedBooking waitlistEntry(WaitlistEntry waitlistEntry) {
        return new CreatedBooking(null, waitlistEntry);
    }
}
