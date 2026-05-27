package com.shootersplatform.backend.identity;

import com.shootersplatform.backend.identity.domain.PasswordResetNotification;
import com.shootersplatform.backend.identity.domain.PasswordResetNotificationGateway;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class InMemoryPasswordResetNotificationGateway implements PasswordResetNotificationGateway {

    private final List<PasswordResetNotification> notifications = new ArrayList<>();

    @Override
    public void send(PasswordResetNotification notification) {
        notifications.add(notification);
    }

    public List<PasswordResetNotification> sent() {
        return List.copyOf(notifications);
    }
}
