package com.shootersplatform.backend.bookings.web;

import jakarta.validation.constraints.NotBlank;

record WaitlistTokenRequest(@NotBlank String token) {
}
