package com.shootersplatform.backend.bookings.reservation.web;

import jakarta.validation.constraints.NotBlank;

record ReservationTokenRequest(@NotBlank String token) {
}
