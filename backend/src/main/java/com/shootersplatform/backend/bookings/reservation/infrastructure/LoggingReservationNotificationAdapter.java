package com.shootersplatform.backend.bookings.reservation.infrastructure;

import com.shootersplatform.backend.bookings.reservation.domain.ReservationNotificationPort;
import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.bookings.term.domain.Term;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@NullMarked
@Component
class LoggingReservationNotificationAdapter implements ReservationNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingReservationNotificationAdapter.class);

    @Override
    public void reservationConfirmed(Term term, Reservation reservation) {
        log.info("Reservation confirmed for term {} and reservation {}", term.id().value(), reservation.id().value());
    }

    @Override
    public void waitlistOfferCreated(Term term, Reservation reservation) {
        log.info("Waitlist offer created for term {} and reservation {}", term.id().value(), reservation.id().value());
    }
}
