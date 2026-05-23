package com.shootersplatform.backend.bookings.reservation.domain;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.identity.domain.EmailAddress;
import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservations;
    private final Clock clock;

    public ReservationService(ReservationRepository reservations, Clock clock) {
        this.reservations = reservations;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Reservation> listReservations(TermId termId) {
        return reservations.findByTerm(termId);
    }

    @Transactional(readOnly = true)
    public long countOccupiedPlaces(TermId termId) {
        return reservations.countOccupiedPlaces(termId);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveReservation(TermId termId, EmailAddress email) {
        return reservations.existsActiveByTermAndEmail(termId, email);
    }

    public Reservation createConfirmed(
            TermId termId,
            @Nullable UserId participantUserId,
            String firstName,
            String lastName,
            String email,
            String phoneNumber
    ) {
        Reservation reservation = Reservation.createConfirmed(termId, participantUserId, firstName, lastName, email, phoneNumber, clock.instant());
        return reservations.save(reservation);
    }

    public Reservation createWaitlistOffer(
            TermId termId,
            @Nullable UserId participantUserId,
            String firstName,
            String lastName,
            EmailAddress email,
            String phoneNumber,
            String cancellationToken,
            java.time.Instant expiresAt
    ) {
        Reservation reservation = Reservation.createWaitlistOffer(
                termId,
                participantUserId,
                firstName,
                lastName,
                email,
                phoneNumber,
                cancellationToken,
                expiresAt,
                clock.instant()
        );
        return reservations.save(reservation);
    }

    @Transactional(readOnly = true)
    public Reservation waitlistOffer(String token) {
        return reservations.findByWaitlistConfirmationToken(token).orElseThrow(ReservationNotFoundException::new);
    }

    public Reservation confirmWaitlistOffer(Reservation reservation, String token) {
        Reservation confirmed = reservation.confirmWaitlistOffer(token, clock.instant());
        return reservations.save(confirmed);
    }

    @Transactional(readOnly = true)
    public Reservation participantCancellation(String cancellationToken) {
        return reservations.findByCancellationToken(cancellationToken).orElseThrow(ReservationNotFoundException::new);
    }

    public Reservation cancelByParticipant(Reservation reservation) {
        return reservations.save(reservation.cancelByParticipant(clock.instant()));
    }

    public Reservation cancelByInstructor(TermId termId, ReservationId reservationId) {
        Reservation reservation = reservations.findByIdAndTerm(reservationId, termId).orElseThrow(ReservationNotFoundException::new);
        return reservations.save(reservation.cancelByInstructor(clock.instant()));
    }

    public int expireWaitlistOffers(TermId termId) {
        int expired = 0;
        for (Reservation reservation : reservations.findExpiredWaitlistOffers(clock.instant())) {
            if (reservation.termId().equals(termId)) {
                reservations.save(reservation.expireWaitlistOffer(clock.instant()));
                expired++;
            }
        }
        return expired;
    }
}
