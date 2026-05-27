package com.shootersplatform.backend.identity;

import com.shootersplatform.backend.identity.domain.EmailAddress;
import com.shootersplatform.backend.identity.domain.LoginRateLimiter;
import com.shootersplatform.backend.identity.domain.PasswordResetRateLimiter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class InMemoryLoginRateLimiter implements LoginRateLimiter, PasswordResetRateLimiter {

    private boolean registrationBlocked;
    private boolean loginBlocked;
    private boolean passwordResetBlocked;
    private int registrationAttempts;
    private int loginFailures;
    private int passwordResetAttempts;

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

    @Override
    public boolean recordPasswordResetRequest(EmailAddress email, String clientIp) {
        if (passwordResetBlocked) {
            return false;
        }
        passwordResetAttempts++;
        return true;
    }

    public void blockRegistration() {
        registrationBlocked = true;
    }

    public void blockLogin() {
        loginBlocked = true;
    }

    public void blockPasswordReset() {
        passwordResetBlocked = true;
    }

    public int registrationAttempts() {
        return registrationAttempts;
    }

    public int loginFailures() {
        return loginFailures;
    }

    public int passwordResetAttempts() {
        return passwordResetAttempts;
    }
}
