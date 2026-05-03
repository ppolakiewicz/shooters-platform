package com.shootersplatform.backend.identity.domain;

public interface LoginRateLimiter {

    void recordRegistrationAttempt(String clientIp);

    void assertLoginAllowed(EmailAddress email, String clientIp);

    void recordLoginFailure(EmailAddress email, String clientIp);

    void clearLoginFailures(EmailAddress email, String clientIp);
}
