package com.shootersplatform.backend.identity.usecase;

import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import com.shootersplatform.backend.identity.domain.EmailAddress;
import com.shootersplatform.backend.identity.domain.IdentityService;
import com.shootersplatform.backend.identity.domain.InvalidCredentialsException;
import com.shootersplatform.backend.identity.domain.LoginRateLimiter;
import com.shootersplatform.backend.identity.domain.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUserUseCase.class);

    private final IdentityService identity;
    private final LoginRateLimiter rateLimiter;

    public LoginUserUseCase(
            IdentityService identity,
            LoginRateLimiter rateLimiter
    ) {
        this.identity = identity;
        this.rateLimiter = rateLimiter;
    }

    @Transactional(readOnly = true)
    public AuthenticatedUser login(String rawEmail, String password, String clientIp) {
        EmailAddress email = new EmailAddress(rawEmail);
        rateLimiter.assertLoginAllowed(email, clientIp);

        try {
            UserAccount account = identity.authenticate(email, password);
            rateLimiter.clearLoginFailures(email, clientIp);
            log.info("Successful login for user {}", account.id());
            return AuthenticatedUser.from(account);
        } catch (InvalidCredentialsException exception) {
            rateLimiter.recordLoginFailure(email, clientIp);
            log.info("Failed login for normalized email {}", email.value());
            throw exception;
        }
    }
}
