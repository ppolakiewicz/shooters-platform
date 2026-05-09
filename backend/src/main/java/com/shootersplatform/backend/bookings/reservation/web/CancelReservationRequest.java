package com.shootersplatform.backend.bookings.reservation.web;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

record CancelReservationRequest(@NotNull UUID termId, @NotNull UUID reservationId) {
}
