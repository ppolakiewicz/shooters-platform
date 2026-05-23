package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.notification.domain.BookingNotification;
import com.shootersplatform.backend.bookings.notification.domain.BookingNotificationService;
import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistEntry;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistService;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;

@Component
class WaitlistPromotionCoordinator {

    private static final Duration WAITLIST_OFFER_TTL = Duration.ofHours(24);

    private final ReservationService reservations;
    private final WaitlistService waitlist;
    private final BookingNotificationService notifications;
    private final Clock clock;

    WaitlistPromotionCoordinator(
            ReservationService reservations,
            WaitlistService waitlist,
            BookingNotificationService notifications,
            Clock clock
    ) {
        this.reservations = reservations;
        this.waitlist = waitlist;
        this.notifications = notifications;
        this.clock = clock;
    }

    void promoteIfPossible(Term term) {
        while (reservations.countOccupiedPlaces(term.id()) < term.capacity()) {
            WaitlistEntry entry = waitlist.pollFirst(term.id()).orElse(null);
            if (entry == null) {
                return;
            }
            Reservation offered = reservations.createWaitlistOffer(
                    term.id(),
                    entry.participantUserId(),
                    entry.firstName(),
                    entry.lastName(),
                    entry.email(),
                    entry.phoneNumber(),
                    entry.cancellationToken(),
                    clock.instant().plus(WAITLIST_OFFER_TTL)
            );
            notifications.send(BookingNotification.waitlistOfferCreated(term, offered));
        }
    }
}
