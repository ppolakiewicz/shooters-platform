package com.shootersplatform.backend.identity.domain;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException() {
        super("Too many attempts. Try again later.");
    }
}
