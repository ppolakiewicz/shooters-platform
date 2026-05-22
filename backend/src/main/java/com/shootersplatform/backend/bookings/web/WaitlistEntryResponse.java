package com.shootersplatform.backend.bookings.web;

import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistEntry;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

record WaitlistEntryResponse(
        UUID id,
        UUID termId,
        @Nullable UUID participantUserId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        int position,
        Instant createdAt,
        Instant updatedAt
) {

    static WaitlistEntryResponse from(WaitlistEntry entry) {
        return new WaitlistEntryResponse(
                entry.id().value(),
                entry.termId().value(),
                entry.participantUserId() == null ? null : entry.participantUserId().value(),
                entry.firstName(),
                entry.lastName(),
                entry.email().value(),
                entry.phoneNumber(),
                entry.position(),
                entry.createdAt(),
                entry.updatedAt()
        );
    }
}
