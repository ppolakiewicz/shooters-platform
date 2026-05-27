package com.shootersplatform.backend.identity.infrastructure;

import com.shootersplatform.backend.identity.domain.PasswordResetNotification;
import com.shootersplatform.backend.identity.domain.PasswordResetNotificationGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingPasswordResetNotificationGateway implements PasswordResetNotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(LoggingPasswordResetNotificationGateway.class);

    @Override
    public void send(PasswordResetNotification notification) {
        log.info("Password reset link for {}: {}", notification.email().value(), notification.resetLink());
    }
}
