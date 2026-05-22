package com.shootersplatform.backend.bookings.web;

import jakarta.validation.constraints.NotBlank;

record ReservationTokenRequest(@NotBlank String token) {
}
