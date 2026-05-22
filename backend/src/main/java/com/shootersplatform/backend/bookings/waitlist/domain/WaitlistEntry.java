package com.shootersplatform.backend.bookings.waitlist.domain;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.identity.domain.EmailAddress;
import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record WaitlistEntry(
        WaitlistEntryId id,
        TermId termId,
        @Nullable UserId participantUserId,
        String firstName,
        String lastName,
        EmailAddress email,
        String phoneNumber,
        int position,
        String cancellationToken,
        Instant createdAt,
        Instant updatedAt
) {

    private static final int NAME_MAX_LENGTH = 80;
    private static final int PHONE_MAX_LENGTH = 40;

    public static WaitlistEntry create(
            TermId termId,
            @Nullable UserId participantUserId,
            String firstName,
            String lastName,
            String rawEmail,
            String phoneNumber,
            int position,
            Instant now
    ) {
        return new WaitlistEntry(
                WaitlistEntryId.newId(),
                termId,
                participantUserId,
                firstName,
                lastName,
                new EmailAddress(rawEmail),
                phoneNumber,
                position,
                newToken(),
                now,
                now
        );
    }

    public WaitlistEntry {
        firstName = normalizeRequired(firstName, "First name", NAME_MAX_LENGTH);
        lastName = normalizeRequired(lastName, "Last name", NAME_MAX_LENGTH);
        phoneNumber = normalizeRequired(phoneNumber, "Phone number", PHONE_MAX_LENGTH);
        if (position < 1) {
            throw new WaitlistValidationException("Waitlist entry requires a positive position");
        }
    }

    public WaitlistEntry withPosition(int newPosition, Instant now) {
        return new WaitlistEntry(id, termId, participantUserId, firstName, lastName, email, phoneNumber, newPosition, cancellationToken, createdAt, now);
    }

    private static String normalizeRequired(@Nullable String value, String fieldName, int maxLength) {
        if (value == null) {
            throw new WaitlistValidationException(fieldName + " is required");
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new WaitlistValidationException(fieldName + " is required");
        }
        if (normalized.length() > maxLength) {
            throw new WaitlistValidationException(fieldName + " cannot exceed " + maxLength + " characters");
        }
        return normalized;
    }

    private static String newToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
