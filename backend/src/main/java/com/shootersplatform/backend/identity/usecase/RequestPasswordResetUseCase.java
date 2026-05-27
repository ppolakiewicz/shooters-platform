package com.shootersplatform.backend.identity.usecase;

import com.shootersplatform.backend.identity.domain.EmailAddress;
import com.shootersplatform.backend.identity.domain.PasswordResetRateLimiter;
import com.shootersplatform.backend.identity.domain.PasswordResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestPasswordResetUseCase {

    private static final Logger log = LoggerFactory.getLogger(RequestPasswordResetUseCase.class);

    private final PasswordResetService passwordReset;
    private final PasswordResetRateLimiter rateLimiter;
    private final String frontendBaseUrl;

    RequestPasswordResetUseCase(
            PasswordResetService passwordReset,
            PasswordResetRateLimiter rateLimiter,
            @Value("${app.frontend-base-url:http://localhost:4200}") String frontendBaseUrl
    ) {
        this.passwordReset = passwordReset;
        this.rateLimiter = rateLimiter;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Transactional
    public void request(String rawEmail, String clientIp) {
        EmailAddress email = new EmailAddress(rawEmail);
        if (!rateLimiter.recordPasswordResetRequest(email, clientIp)) {
            log.info("Password reset request rate limited for normalized email {}", email.value());
            return;
        }

        passwordReset.requestReset(email.value(), frontendBaseUrl);
    }
}
