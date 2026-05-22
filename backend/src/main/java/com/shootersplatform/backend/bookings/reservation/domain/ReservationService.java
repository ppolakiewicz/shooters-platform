package com.shootersplatform.backend.bookings.reservation.domain;

import com.shootersplatform.backend.identity.domain.EmailAddress;
import com.shootersplatform.backend.identity.domain.UserId;
import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.term.domain.TermRepository;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistEntry;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistRepository;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistService;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

@Service
@Transactional
public class ReservationService {

    private static final Duration WAITLIST_OFFER_TTL = Duration.ofHours(24);

    private final TermRepository terms;
    private final ReservationRepository reservations;
    private final WaitlistRepository waitlist;
    private final WaitlistService waitlistService;
    private final ReservationNotificationPort notifications;
    private final Clock clock;

    public ReservationService(
            TermRepository terms,
            ReservationRepository reservations,
            WaitlistRepository waitlist,
            WaitlistService waitlistService,
            ReservationNotificationPort notifications,
            Clock clock
    ) {
        this.terms = terms;
        this.reservations = reservations;
        this.waitlist = waitlist;
        this.waitlistService = waitlistService;
        this.notifications = notifications;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Reservation> listReservations(UserId ownerId, TermId termId) {
        Term term = terms.findByIdAndOwner(termId, ownerId).orElseThrow(ReservationNotFoundException::new);
        return reservations.findByTerm(term.id());
    }

    @Transactional(readOnly = true)
    public long countOccupiedPlaces(TermId termId) {
        return reservations.countOccupiedPlaces(termId);
    }

    public CreatedBooking createReservation(
            TermId termId,
            @Nullable UserId participantUserId,
            String firstName,
            String lastName,
            String email,
            String phoneNumber
    ) {
        Term lockedTerm = terms.findByIdForUpdate(termId).orElseThrow(ReservationNotFoundException::new);
        if (!lockedTerm.startsInFuture(clock.instant())) {
            throw new ReservationValidationException("Term has already started");
        }
        EmailAddress emailAddress = new EmailAddress(email);
        if (reservations.existsActiveByTermAndEmail(termId, emailAddress) || waitlist.existsByTermAndEmail(termId, emailAddress)) {
            throw new ReservationValidationException("Participant is already registered for this term");
        }

        if (reservations.countOccupiedPlaces(termId) < lockedTerm.capacity()) {
            Reservation reservation = Reservation.createConfirmed(termId, participantUserId, firstName, lastName, email, phoneNumber, clock.instant());
            reservation = reservations.save(reservation);
            notifications.reservationConfirmed(lockedTerm, reservation);
            return CreatedBooking.reservation(reservation);
        }

        WaitlistEntry entry = waitlistService.add(termId, participantUserId, firstName, lastName, email, phoneNumber);
        return CreatedBooking.waitlistEntry(entry);
    }

    public Reservation confirmWaitlistOffer(String token) {
        Reservation reservation = reservations.findByWaitlistConfirmationToken(token).orElseThrow(ReservationNotFoundException::new);
        Term term = terms.findByIdForUpdate(reservation.termId()).orElseThrow(ReservationNotFoundException::new);
        Reservation confirmed = reservation.confirmWaitlistOffer(token, clock.instant());
        if (reservations.countOccupiedPlaces(term.id()) > term.capacity()) {
            throw new ReservationValidationException("Term capacity has already been reached");
        }
        return reservations.save(confirmed);
    }

    public Reservation cancelByParticipant(String cancellationToken) {
        Reservation reservation = reservations.findByCancellationToken(cancellationToken).orElseThrow(ReservationNotFoundException::new);
        Term term = terms.findByIdForUpdate(reservation.termId()).orElseThrow(ReservationNotFoundException::new);
        if (!term.canParticipantCancel(clock.instant())) {
            throw new ReservationValidationException("Cancellation deadline has passed");
        }
        Reservation cancelled = reservations.save(reservation.cancelByParticipant(clock.instant()));
        promoteWaitlistedIfPossible(term);
        return cancelled;
    }

    public Reservation cancelByInstructor(UserId ownerId, TermId termId, ReservationId reservationId) {
        Term term = terms.findByIdForUpdate(termId).orElseThrow(ReservationNotFoundException::new);
        if (!term.ownerId().equals(ownerId)) {
            throw new ReservationNotFoundException();
        }
        Reservation reservation = reservations.findByIdAndTerm(reservationId, termId).orElseThrow(ReservationNotFoundException::new);
        Reservation cancelled = reservations.save(reservation.cancelByInstructor(clock.instant()));
        promoteWaitlistedIfPossible(term);
        return cancelled;
    }

    public int expireWaitlistOffers(UserId ownerId, TermId termId) {
        Term term = terms.findByIdForUpdate(termId).orElseThrow(ReservationNotFoundException::new);
        if (!term.ownerId().equals(ownerId)) {
            throw new ReservationNotFoundException();
        }
        int expired = 0;
        for (Reservation reservation : reservations.findExpiredWaitlistOffers(clock.instant())) {
            if (reservation.termId().equals(termId)) {
                reservations.save(reservation.expireWaitlistOffer(clock.instant()));
                expired++;
            }
        }
        if (expired > 0) {
            promoteWaitlistedIfPossible(term);
        }
        return expired;
    }

    private void promoteWaitlistedIfPossible(Term term) {
        while (reservations.countOccupiedPlaces(term.id()) < term.capacity()) {
            WaitlistEntry entry = waitlistService.pollFirst(term.id()).orElse(null);
            if (entry == null) {
                return;
            }
            Reservation offered = Reservation.createWaitlistOffer(entry, clock.instant().plus(WAITLIST_OFFER_TTL), clock.instant());
            Reservation saved = reservations.save(offered);
            notifications.waitlistOfferCreated(term, saved);
        }
    }
}
