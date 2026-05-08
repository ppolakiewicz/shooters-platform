package com.shootersplatform.backend.identity.domain;

public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException() {
        super("Username is already registered");
    }
}
