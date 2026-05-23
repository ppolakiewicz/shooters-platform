package com.shootersplatform.backend.bookings.notification

import com.shootersplatform.backend.bookings.notification.domain.BookingNotificationService
import com.shootersplatform.backend.bookings.notification.domain.InMemoryBookingNotificationGateway

class BookingNotificationServiceTestConfiguration {

    private BookingNotificationServiceTestConfiguration() {
    }

    static BookingNotificationService inMemory() {
        return new BookingNotificationService(
                new InMemoryBookingNotificationGateway()
        )
    }
}
