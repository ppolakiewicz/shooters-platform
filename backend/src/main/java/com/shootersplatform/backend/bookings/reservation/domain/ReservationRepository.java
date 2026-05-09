package com.shootersplatform.backend.bookings.reservation.domain;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.identity.domain.EmailAddress;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findByTerm(TermId termId);

    Optional<Reservation> findByIdAndTerm(ReservationId reservationId, TermId termId);

    Optional<Reservation> findByCancellationToken(String token);

    Optional<Reservation> findByWaitlistConfirmationToken(String token);

    Optional<Reservation> findFirstWaitlisted(TermId termId);

    List<Reservation> findExpiredWaitlistOffers(Instant now);

    boolean existsActiveByTermAndEmail(TermId termId, EmailAddress email);

    long countOccupiedPlaces(TermId termId);

    int nextWaitlistPosition(TermId termId);

    Reservation save(Reservation reservation);
}
