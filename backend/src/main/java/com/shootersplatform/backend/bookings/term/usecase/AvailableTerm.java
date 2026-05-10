package com.shootersplatform.backend.bookings.term.usecase;

import com.shootersplatform.backend.bookings.term.domain.Term;

public record AvailableTerm(Term term, int availablePlaces) {
}
