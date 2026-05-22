package com.shootersplatform.backend.bookings.waitlist.web;

import jakarta.validation.constraints.NotBlank;

record WaitlistTokenRequest(@NotBlank String token) {
}
