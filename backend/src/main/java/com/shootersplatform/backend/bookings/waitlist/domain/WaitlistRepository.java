package com.shootersplatform.backend.bookings.waitlist.domain;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.identity.domain.EmailAddress;

import java.util.List;
import java.util.Optional;

public interface WaitlistRepository {

    List<WaitlistEntry> findByTerm(TermId termId);

    Optional<WaitlistEntry> findByIdAndTerm(WaitlistEntryId entryId, TermId termId);

    Optional<WaitlistEntry> findByCancellationToken(String token);

    Optional<WaitlistEntry> findFirstByTerm(TermId termId);

    boolean existsByTermAndEmail(TermId termId, EmailAddress email);

    int nextPosition(TermId termId);

    WaitlistEntry save(WaitlistEntry entry);

    void delete(WaitlistEntry entry);
}
