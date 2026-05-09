package com.shootersplatform.backend.bookings.term.domain;

import com.shootersplatform.backend.identity.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface TermRepository {

    List<Term> findPublicTerms();

    List<Term> findByOwner(UserId ownerId);

    Optional<Term> findById(TermId id);

    Optional<Term> findByIdAndOwner(TermId id, UserId ownerId);

    Optional<Term> findByIdForUpdate(TermId id);

    Term save(Term term);
}
