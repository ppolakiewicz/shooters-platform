package com.shootersplatform.backend.identity;

import com.shootersplatform.backend.identity.domain.EmailAddress;
import com.shootersplatform.backend.identity.domain.LoginRateLimiter;

public class InMemoryLoginRateLimiter implements LoginRateLimiter {

    private boolean registrationBlocked;
    private boolean loginBlocked;
    private int registrationAttempts;
    private int loginFailures;

    @Override
    public void recordRegistrationAttempt(String clientIp) {
        if (registrationBlocked) {
            throw new com.shootersplatform.backend.identity.domain.RateLimitExceededException();
        }
        registrationAttempts++;
    }

    @Override
    public void assertLoginAllowed(EmailAddress email, String clientIp) {
        if (loginBlocked) {
            throw new com.shootersplatform.backend.identity.domain.RateLimitExceededException();
        }
    }

    @Override
    public void recordLoginFailure(EmailAddress email, String clientIp) {
        loginFailures++;
    }

    @Override
    public void clearLoginFailures(EmailAddress email, String clientIp) {
        loginFailures = 0;
    }

    public void blockRegistration() {
        registrationBlocked = true;
    }

    public void blockLogin() {
        loginBlocked = true;
    }

    public int registrationAttempts() {
        return registrationAttempts;
    }

    public int loginFailures() {
        return loginFailures;
    }
}
