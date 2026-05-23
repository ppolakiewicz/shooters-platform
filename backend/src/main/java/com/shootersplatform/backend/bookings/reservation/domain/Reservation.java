package com.shootersplatform.backend.bookings.reservation.domain;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.identity.domain.EmailAddress;
import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record Reservation(
        ReservationId id,
        TermId termId,
        @Nullable UserId participantUserId,
        String firstName,
        String lastName,
        EmailAddress email,
        String phoneNumber,
        ReservationStatus status,
        String cancellationToken,
        @Nullable String waitlistConfirmationToken,
        @Nullable Instant waitlistOfferExpiresAt,
        Instant createdAt,
        Instant updatedAt
) {

    private static final int NAME_MAX_LENGTH = 80;
    private static final int PHONE_MAX_LENGTH = 40;

    public static Reservation createConfirmed(
            TermId termId,
            @Nullable UserId participantUserId,
            String firstName,
            String lastName,
            String rawEmail,
            String phoneNumber,
            Instant now
    ) {
        return create(termId, participantUserId, firstName, lastName, rawEmail, phoneNumber, ReservationStatus.CONFIRMED, now);
    }

    public static Reservation createWaitlistOffer(
            TermId termId,
            @Nullable UserId participantUserId,
            String firstName,
            String lastName,
            EmailAddress email,
            String phoneNumber,
            String cancellationToken,
            Instant expiresAt,
            Instant now
    ) {
        return new Reservation(
                ReservationId.newId(),
                termId,
                participantUserId,
                firstName,
                lastName,
                email,
                phoneNumber,
                ReservationStatus.WAITLIST_OFFERED,
                cancellationToken,
                newToken(),
                expiresAt,
                now,
                now
        );
    }

    private static Reservation create(
            TermId termId,
            @Nullable UserId participantUserId,
            String firstName,
            String lastName,
            String rawEmail,
            String phoneNumber,
            ReservationStatus status,
            Instant now
    ) {
        return new Reservation(
                ReservationId.newId(),
                termId,
                participantUserId,
                firstName,
                lastName,
                new EmailAddress(rawEmail),
                phoneNumber,
                status,
                newToken(),
                null,
                null,
                now,
                now
        );
    }

    public Reservation {
        firstName = normalizeRequired(firstName, "First name", NAME_MAX_LENGTH);
        lastName = normalizeRequired(lastName, "Last name", NAME_MAX_LENGTH);
        phoneNumber = normalizeRequired(phoneNumber, "Phone number", PHONE_MAX_LENGTH);
    }

    public boolean occupiesPlace() {
        return status == ReservationStatus.CONFIRMED || status == ReservationStatus.WAITLIST_OFFERED;
    }

    public boolean activeForDuplicateCheck() {
        return status == ReservationStatus.CONFIRMED || status == ReservationStatus.WAITLIST_OFFERED;
    }

    public Reservation confirmWaitlistOffer(String token, Instant now) {
        if (status != ReservationStatus.WAITLIST_OFFERED || waitlistConfirmationToken == null || !waitlistConfirmationToken.equals(token)) {
            throw new ReservationValidationException("Waitlist confirmation link is invalid");
        }
        if (waitlistOfferExpiresAt == null || now.isAfter(waitlistOfferExpiresAt)) {
            throw new ReservationValidationException("Waitlist confirmation link has expired");
        }
        return new Reservation(id, termId, participantUserId, firstName, lastName, email, phoneNumber, ReservationStatus.CONFIRMED, cancellationToken, null, null, createdAt, now);
    }

    public Reservation cancelByParticipant(Instant now) {
        return cancel(ReservationStatus.CANCELLED_BY_PARTICIPANT, now);
    }

    public Reservation cancelByInstructor(Instant now) {
        return cancel(ReservationStatus.CANCELLED_BY_INSTRUCTOR, now);
    }

    public Reservation expireWaitlistOffer(Instant now) {
        if (status != ReservationStatus.WAITLIST_OFFERED) {
            throw new ReservationValidationException("Only waitlist offers can expire");
        }
        return new Reservation(id, termId, participantUserId, firstName, lastName, email, phoneNumber, ReservationStatus.WAITLIST_OFFER_EXPIRED, cancellationToken, null, null, createdAt, now);
    }

    private Reservation cancel(ReservationStatus cancelledStatus, Instant now) {
        if (!activeForDuplicateCheck()) {
            throw new ReservationValidationException("Reservation is already closed");
        }
        return new Reservation(id, termId, participantUserId, firstName, lastName, email, phoneNumber, cancelledStatus, cancellationToken, waitlistConfirmationToken, waitlistOfferExpiresAt, createdAt, now);
    }

    private static String normalizeRequired(@Nullable String value, String fieldName, int maxLength) {
        if (value == null) {
            throw new ReservationValidationException(fieldName + " is required");
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new ReservationValidationException(fieldName + " is required");
        }
        if (normalized.length() > maxLength) {
            throw new ReservationValidationException(fieldName + " cannot exceed " + maxLength + " characters");
        }
        return normalized;
    }

    private static String newToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
