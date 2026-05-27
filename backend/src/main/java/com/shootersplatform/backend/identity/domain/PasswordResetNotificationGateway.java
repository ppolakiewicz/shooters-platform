package com.shootersplatform.backend.identity.domain;

public interface PasswordResetNotificationGateway {

    void send(PasswordResetNotification notification);
}
