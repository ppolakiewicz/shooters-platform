package com.shootersplatform.backend.bookings.term.domain;

import java.util.UUID;

public record TermId(UUID value) {

    public static TermId newId() {
        return new TermId(UUID.randomUUID());
    }
}
