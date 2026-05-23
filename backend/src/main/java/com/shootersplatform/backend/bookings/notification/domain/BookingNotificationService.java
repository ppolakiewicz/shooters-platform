package com.shootersplatform.backend.bookings.notification.domain;

import org.springframework.stereotype.Service;

@Service
public class BookingNotificationService {

    private final BookingNotificationGateway gateway;

    public BookingNotificationService(BookingNotificationGateway gateway) {
        this.gateway = gateway;
    }

    public void send(BookingNotification notification) {
        gateway.send(notification);
    }
}
