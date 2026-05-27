package com.shootersplatform.backend.identity.infrastructure;

import com.shootersplatform.backend.identity.domain.EmailAddress;
import com.shootersplatform.backend.identity.domain.LoginRateLimiter;
import com.shootersplatform.backend.identity.domain.PasswordResetRateLimiter;
import com.shootersplatform.backend.identity.domain.RateLimitExceededException;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
@Component
class InMemoryLoginRateLimiter implements LoginRateLimiter, PasswordResetRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(InMemoryLoginRateLimiter.class);
    private static final int LOGIN_FAILURE_LIMIT = 5;
    private static final int REGISTRATION_LIMIT = 5;
    private static final int PASSWORD_RESET_LIMIT = 3;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(15);
    private static final Duration REGISTRATION_WINDOW = Duration.ofHours(1);
    private static final Duration PASSWORD_RESET_WINDOW = Duration.ofMinutes(15);

    private final Map<String, Deque<Instant>> loginFailures = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> registrationAttempts = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> passwordResetAttempts = new ConcurrentHashMap<>();
    private final Clock clock;

    InMemoryLoginRateLimiter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void recordRegistrationAttempt(String clientIp) {
        record(registrationAttempts, "registration:%s".formatted(normalize(clientIp)), REGISTRATION_LIMIT, REGISTRATION_WINDOW);
    }

    @Override
    public void assertLoginAllowed(EmailAddress email, String clientIp) {
        String key = loginKey(email, clientIp);
        Deque<Instant> attempts = loginFailures.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (attempts) {
            prune(attempts, LOGIN_WINDOW);
            if (attempts.size() >= LOGIN_FAILURE_LIMIT) {
                log.info("Rate limit hit for login key {}", key);
                throw new RateLimitExceededException();
            }
        }
    }

    @Override
    public void recordLoginFailure(EmailAddress email, String clientIp) {
        record(loginFailures, loginKey(email, clientIp), LOGIN_FAILURE_LIMIT + 1, LOGIN_WINDOW);
    }

    @Override
    public void clearLoginFailures(EmailAddress email, String clientIp) {
        loginFailures.remove(loginKey(email, clientIp));
    }

    @Override
    public boolean recordPasswordResetRequest(EmailAddress email, String clientIp) {
        return tryRecord(passwordResetAttempts, passwordResetKey(email, clientIp), PASSWORD_RESET_LIMIT, PASSWORD_RESET_WINDOW);
    }

    private void record(Map<String, Deque<Instant>> attemptsByKey, String key, int limit, Duration window) {
        if (!tryRecord(attemptsByKey, key, limit, window)) {
            throw new RateLimitExceededException();
        }
    }

    private boolean tryRecord(Map<String, Deque<Instant>> attemptsByKey, String key, int limit, Duration window) {
        Deque<Instant> attempts = attemptsByKey.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (attempts) {
            prune(attempts, window);
            if (attempts.size() >= limit) {
                log.info("Rate limit hit for key {}", key);
                return false;
            }
            attempts.addLast(clock.instant());
            return true;
        }
    }

    private void prune(Deque<Instant> attempts, Duration window) {
        Instant oldestAllowed = clock.instant().minus(window);
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(oldestAllowed)) {
            attempts.removeFirst();
        }
    }

    private String loginKey(EmailAddress email, String clientIp) {
        return "login:%s:%s".formatted(email.value(), normalize(clientIp));
    }

    private String passwordResetKey(EmailAddress email, String clientIp) {
        return "password-reset:%s:%s".formatted(email.value(), normalize(clientIp));
    }

    private String normalize(@Nullable String clientIp) {
        return clientIp == null ? "unknown" : clientIp.toLowerCase(Locale.ROOT);
    }
}
