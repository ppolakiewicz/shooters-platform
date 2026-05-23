package com.shootersplatform.backend.bookings.notification.domain;

public interface BookingNotificationGateway {

    void send(BookingNotification notification);
}
