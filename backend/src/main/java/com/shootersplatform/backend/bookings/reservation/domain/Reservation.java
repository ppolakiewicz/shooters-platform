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
        int waitlistPosition,
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
        return create(termId, participantUserId, firstName, lastName, rawEmail, phoneNumber, ReservationStatus.CONFIRMED, 0, now);
    }

    public static Reservation createWaitlisted(
            TermId termId,
            @Nullable UserId participantUserId,
            String firstName,
            String lastName,
            String rawEmail,
            String phoneNumber,
            int waitlistPosition,
            Instant now
    ) {
        return create(termId, participantUserId, firstName, lastName, rawEmail, phoneNumber, ReservationStatus.WAITLISTED, waitlistPosition, now);
    }

    private static Reservation create(
            TermId termId,
            @Nullable UserId participantUserId,
            String firstName,
            String lastName,
            String rawEmail,
            String phoneNumber,
            ReservationStatus status,
            int waitlistPosition,
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
                waitlistPosition,
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
        if (waitlistPosition < 0) {
            throw new ReservationValidationException("Waitlist position cannot be negative");
        }
        if (status == ReservationStatus.WAITLISTED && waitlistPosition < 1) {
            throw new ReservationValidationException("Waitlisted reservation requires a waitlist position");
        }
    }

    public boolean occupiesPlace() {
        return status == ReservationStatus.CONFIRMED || status == ReservationStatus.WAITLIST_OFFERED;
    }

    public boolean activeForDuplicateCheck() {
        return status == ReservationStatus.CONFIRMED || status == ReservationStatus.WAITLISTED || status == ReservationStatus.WAITLIST_OFFERED;
    }

    public Reservation offerWaitlistPlace(Instant expiresAt, Instant now) {
        if (status != ReservationStatus.WAITLISTED) {
            throw new ReservationValidationException("Only waitlisted reservation can receive a waitlist offer");
        }
        return new Reservation(id, termId, participantUserId, firstName, lastName, email, phoneNumber, ReservationStatus.WAITLIST_OFFERED, waitlistPosition, cancellationToken, newToken(), expiresAt, createdAt, now);
    }

    public Reservation confirmWaitlistOffer(String token, Instant now) {
        if (status != ReservationStatus.WAITLIST_OFFERED || waitlistConfirmationToken == null || !waitlistConfirmationToken.equals(token)) {
            throw new ReservationValidationException("Waitlist confirmation link is invalid");
        }
        if (waitlistOfferExpiresAt == null || now.isAfter(waitlistOfferExpiresAt)) {
            throw new ReservationValidationException("Waitlist confirmation link has expired");
        }
        return new Reservation(id, termId, participantUserId, firstName, lastName, email, phoneNumber, ReservationStatus.CONFIRMED, 0, cancellationToken, null, null, createdAt, now);
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
        return new Reservation(id, termId, participantUserId, firstName, lastName, email, phoneNumber, ReservationStatus.WAITLIST_OFFER_EXPIRED, waitlistPosition, cancellationToken, null, null, createdAt, now);
    }

    private Reservation cancel(ReservationStatus cancelledStatus, Instant now) {
        if (!activeForDuplicateCheck()) {
            throw new ReservationValidationException("Reservation is already closed");
        }
        return new Reservation(id, termId, participantUserId, firstName, lastName, email, phoneNumber, cancelledStatus, waitlistPosition, cancellationToken, waitlistConfirmationToken, waitlistOfferExpiresAt, createdAt, now);
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
