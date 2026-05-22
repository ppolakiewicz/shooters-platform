package com.shootersplatform.backend.bookings.waitlist.domain;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.identity.domain.EmailAddress;
import org.jspecify.annotations.NullMarked;

import java.util.*;

@NullMarked
public class InMemoryWaitlistRepository implements WaitlistRepository {

    private final Map<WaitlistEntryId, WaitlistEntry> entries = new HashMap<>();

    @Override
    public List<WaitlistEntry> findByTerm(TermId termId) {
        return entries.values().stream()
                .filter(entry -> entry.termId().equals(termId))
                .sorted(Comparator.comparingInt(WaitlistEntry::position).thenComparing(WaitlistEntry::createdAt))
                .toList();
    }

    @Override
    public Optional<WaitlistEntry> findByIdAndTerm(WaitlistEntryId entryId, TermId termId) {
        return Optional.ofNullable(entries.get(entryId)).filter(entry -> entry.termId().equals(termId));
    }

    @Override
    public Optional<WaitlistEntry> findByCancellationToken(String token) {
        return entries.values().stream().filter(entry -> entry.cancellationToken().equals(token)).findFirst();
    }

    @Override
    public Optional<WaitlistEntry> findFirstByTerm(TermId termId) {
        return findByTerm(termId).stream().findFirst();
    }

    @Override
    public boolean existsByTermAndEmail(TermId termId, EmailAddress email) {
        return entries.values().stream()
                .filter(entry -> entry.termId().equals(termId))
                .anyMatch(entry -> entry.email().equals(email));
    }

    @Override
    public int nextPosition(TermId termId) {
        return entries.values().stream()
                .filter(entry -> entry.termId().equals(termId))
                .mapToInt(WaitlistEntry::position)
                .max()
                .orElse(0) + 1;
    }

    @Override
    public WaitlistEntry save(WaitlistEntry entry) {
        entries.put(entry.id(), entry);
        return entry;
    }

    @Override
    public void delete(WaitlistEntry entry) {
        entries.remove(entry.id());
    }
}
