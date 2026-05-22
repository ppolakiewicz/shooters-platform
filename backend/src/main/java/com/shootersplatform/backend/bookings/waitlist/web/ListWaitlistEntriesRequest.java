package com.shootersplatform.backend.bookings.waitlist.web;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

record ListWaitlistEntriesRequest(@NotNull UUID termId) {
}
