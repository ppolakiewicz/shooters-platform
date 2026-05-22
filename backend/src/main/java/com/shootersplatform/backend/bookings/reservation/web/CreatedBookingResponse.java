package com.shootersplatform.backend.bookings.reservation.web;

import com.shootersplatform.backend.bookings.reservation.domain.CreatedBooking;
import com.shootersplatform.backend.bookings.waitlist.web.CreatedWaitlistEntryResponse;
import org.jspecify.annotations.Nullable;

record CreatedBookingResponse(
        CreatedBookingType type,
        @Nullable CreatedReservationResponse reservation,
        @Nullable CreatedWaitlistEntryResponse waitlistEntry
) {

    static CreatedBookingResponse from(CreatedBooking created) {
        if (created.reservation() != null) {
            return new CreatedBookingResponse(CreatedBookingType.RESERVATION, CreatedReservationResponse.from(created.reservation()), null);
        }
        if (created.waitlistEntry() != null) {
            return new CreatedBookingResponse(CreatedBookingType.WAITLIST_ENTRY, null, CreatedWaitlistEntryResponse.from(created.waitlistEntry()));
        }
        throw new IllegalArgumentException("Created booking must contain a reservation or waitlist entry");
    }
}
