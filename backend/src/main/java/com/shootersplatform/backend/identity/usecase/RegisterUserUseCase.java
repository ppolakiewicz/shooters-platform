package com.shootersplatform.backend.identity.usecase;

import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import com.shootersplatform.backend.identity.domain.IdentityService;
import com.shootersplatform.backend.identity.domain.LoginRateLimiter;
import com.shootersplatform.backend.identity.domain.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserUseCase.class);

    private final IdentityService identity;
    private final LoginRateLimiter rateLimiter;

    public RegisterUserUseCase(
            IdentityService identity,
            LoginRateLimiter rateLimiter
    ) {
        this.identity = identity;
        this.rateLimiter = rateLimiter;
    }

    @Transactional
    public AuthenticatedUser register(String rawEmail, String rawUsername, String password, String clientIp) {
        rateLimiter.recordRegistrationAttempt(clientIp);

        UserAccount saved = identity.register(rawEmail, rawUsername, password);
        log.info("Registered user {}", saved.id().value());
        return AuthenticatedUser.from(saved);
    }
}
