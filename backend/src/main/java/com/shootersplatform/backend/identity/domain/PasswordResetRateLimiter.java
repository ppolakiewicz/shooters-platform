package com.shootersplatform.backend.identity.domain;

public interface PasswordResetRateLimiter {

    boolean recordPasswordResetRequest(EmailAddress email, String clientIp);
}
