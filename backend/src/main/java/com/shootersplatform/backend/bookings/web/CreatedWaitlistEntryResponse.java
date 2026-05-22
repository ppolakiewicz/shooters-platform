package com.shootersplatform.backend.bookings.web;

import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistEntry;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

record CreatedWaitlistEntryResponse(
        UUID id,
        UUID termId,
        @Nullable UUID participantUserId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        int position,
        String cancellationToken,
        Instant createdAt,
        Instant updatedAt
) {

    static CreatedWaitlistEntryResponse from(WaitlistEntry entry) {
        return new CreatedWaitlistEntryResponse(
                entry.id().value(),
                entry.termId().value(),
                entry.participantUserId() == null ? null : entry.participantUserId().value(),
                entry.firstName(),
                entry.lastName(),
                entry.email().value(),
                entry.phoneNumber(),
                entry.position(),
                entry.cancellationToken(),
                entry.createdAt(),
                entry.updatedAt()
        );
    }
}
