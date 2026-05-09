package com.shootersplatform.backend.bookings;

import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.term.domain.TermRepository;
import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.NullMarked;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@NullMarked
public class InMemoryTermRepository implements TermRepository {

    private final Map<TermId, Term> terms = new HashMap<>();

    @Override
    public List<Term> findPublicTerms() {
        return terms.values().stream()
                .sorted(Comparator.comparing(Term::startsAt).thenComparing(Term::createdAt, Comparator.reverseOrder()))
                .toList();
    }

    @Override
    public List<Term> findByOwner(UserId ownerId) {
        return terms.values().stream()
                .filter(term -> term.ownerId().equals(ownerId))
                .sorted(Comparator.comparing(Term::startsAt))
                .toList();
    }

    @Override
    public Optional<Term> findById(TermId id) {
        return Optional.ofNullable(terms.get(id));
    }

    @Override
    public Optional<Term> findByIdAndOwner(TermId id, UserId ownerId) {
        return Optional.ofNullable(terms.get(id)).filter(term -> term.ownerId().equals(ownerId));
    }

    @Override
    public Optional<Term> findByIdForUpdate(TermId id) {
        return findById(id);
    }

    @Override
    public Term save(Term term) {
        terms.put(term.id(), term);
        return term;
    }
}
