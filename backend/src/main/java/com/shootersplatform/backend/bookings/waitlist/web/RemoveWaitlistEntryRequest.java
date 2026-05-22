package com.shootersplatform.backend.bookings.waitlist.web;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

record RemoveWaitlistEntryRequest(
        @NotNull UUID termId,
        @NotNull UUID waitlistEntryId
) {
}
