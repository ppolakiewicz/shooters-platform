package com.shootersplatform.backend.bookings.reservation.web;

import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationStatus;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

record CreatedReservationResponse(
        UUID id,
        UUID termId,
        @Nullable UUID participantUserId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        ReservationStatus status,
        int waitlistPosition,
        String cancellationToken,
        @Nullable Instant waitlistOfferExpiresAt,
        Instant createdAt,
        Instant updatedAt
) {

    static CreatedReservationResponse from(Reservation reservation) {
        return new CreatedReservationResponse(
                reservation.id().value(),
                reservation.termId().value(),
                reservation.participantUserId() == null ? null : reservation.participantUserId().value(),
                reservation.firstName(),
                reservation.lastName(),
                reservation.email().value(),
                reservation.phoneNumber(),
                reservation.status(),
                reservation.waitlistPosition(),
                reservation.cancellationToken(),
                reservation.waitlistOfferExpiresAt(),
                reservation.createdAt(),
                reservation.updatedAt()
        );
    }
}
