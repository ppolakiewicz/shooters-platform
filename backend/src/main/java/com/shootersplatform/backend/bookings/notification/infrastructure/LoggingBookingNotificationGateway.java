package com.shootersplatform.backend.bookings.notification.infrastructure;

import com.shootersplatform.backend.bookings.notification.domain.BookingNotification;
import com.shootersplatform.backend.bookings.notification.domain.BookingNotificationGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingBookingNotificationGateway implements BookingNotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(LoggingBookingNotificationGateway.class);

    @Override
    public void send(BookingNotification notification) {
        log.info(
                "Booking notification {} for term {} and reservation {}",
                notification.type(),
                notification.term().id().value(),
                notification.reservation().id().value()
        );
    }
}
