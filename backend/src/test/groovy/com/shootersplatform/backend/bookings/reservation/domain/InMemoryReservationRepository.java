package com.shootersplatform.backend.bookings.reservation.domain;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.identity.domain.EmailAddress;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@NullMarked
public class InMemoryReservationRepository implements ReservationRepository {

    private final Map<ReservationId, Reservation> reservations = new HashMap<>();

    @Override
    public List<Reservation> findByTerm(TermId termId) {
        return reservations.values().stream()
                .filter(reservation -> reservation.termId().equals(termId))
                .sorted(Comparator.comparing(Reservation::createdAt))
                .toList();
    }

    @Override
    public Optional<Reservation> findByIdAndTerm(ReservationId reservationId, TermId termId) {
        return Optional.ofNullable(reservations.get(reservationId)).filter(reservation -> reservation.termId().equals(termId));
    }

    @Override
    public Optional<Reservation> findByCancellationToken(String token) {
        return reservations.values().stream().filter(reservation -> reservation.cancellationToken().equals(token)).findFirst();
    }

    @Override
    public Optional<Reservation> findByWaitlistConfirmationToken(String token) {
        return reservations.values().stream()
                .filter(reservation -> token.equals(reservation.waitlistConfirmationToken()))
                .findFirst();
    }

    @Override
    public Optional<Reservation> findFirstWaitlisted(TermId termId) {
        return reservations.values().stream()
                .filter(reservation -> reservation.termId().equals(termId))
                .filter(reservation -> reservation.status() == ReservationStatus.WAITLISTED)
                .min(Comparator.comparingInt(Reservation::waitlistPosition).thenComparing(Reservation::createdAt));
    }

    @Override
    public List<Reservation> findExpiredWaitlistOffers(Instant now) {
        return reservations.values().stream()
                .filter(reservation -> reservation.status() == ReservationStatus.WAITLIST_OFFERED)
                .filter(reservation -> reservation.waitlistOfferExpiresAt() != null && !reservation.waitlistOfferExpiresAt().isAfter(now))
                .toList();
    }

    @Override
    public boolean existsActiveByTermAndEmail(TermId termId, EmailAddress email) {
        return reservations.values().stream()
                .filter(reservation -> reservation.termId().equals(termId))
                .filter(reservation -> reservation.email().equals(email))
                .anyMatch(Reservation::activeForDuplicateCheck);
    }

    @Override
    public long countOccupiedPlaces(TermId termId) {
        return reservations.values().stream()
                .filter(reservation -> reservation.termId().equals(termId))
                .filter(Reservation::occupiesPlace)
                .count();
    }

    @Override
    public int nextWaitlistPosition(TermId termId) {
        return reservations.values().stream()
                .filter(reservation -> reservation.termId().equals(termId))
                .mapToInt(Reservation::waitlistPosition)
                .max()
                .orElse(0) + 1;
    }

    @Override
    public Reservation save(Reservation reservation) {
        reservations.put(reservation.id(), reservation);
        return reservation;
    }
}
