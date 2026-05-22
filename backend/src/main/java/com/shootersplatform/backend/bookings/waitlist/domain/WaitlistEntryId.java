package com.shootersplatform.backend.bookings.waitlist.domain;

import java.util.UUID;

public record WaitlistEntryId(UUID value) {

    public static WaitlistEntryId newId() {
        return new WaitlistEntryId(UUID.randomUUID());
    }
}
