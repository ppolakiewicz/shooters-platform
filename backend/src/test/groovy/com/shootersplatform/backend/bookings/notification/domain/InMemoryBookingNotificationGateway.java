package com.shootersplatform.backend.bookings.notification.domain;

import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class InMemoryBookingNotificationGateway implements BookingNotificationGateway {

    private final List<BookingNotification> notifications = new ArrayList<>();

    @Override
    public void send(BookingNotification notification) {
        notifications.add(notification);
    }

    public List<BookingNotification> sent() {
        return List.copyOf(notifications);
    }

    public List<BookingNotification> confirmedReservations() {
        return notifications.stream()
                .filter(notification -> notification.type() == BookingNotificationType.RESERVATION_CONFIRMED)
                .toList();
    }

    public List<BookingNotification> waitlistOffers() {
        return notifications.stream()
                .filter(notification -> notification.type() == BookingNotificationType.WAITLIST_OFFER_CREATED)
                .toList();
    }
}
