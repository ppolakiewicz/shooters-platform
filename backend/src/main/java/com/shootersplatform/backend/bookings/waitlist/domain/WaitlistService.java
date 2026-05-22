package com.shootersplatform.backend.bookings.waitlist.domain;

import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.term.domain.TermRepository;
import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WaitlistService {

    private final TermRepository terms;
    private final WaitlistRepository waitlist;
    private final Clock clock;

    public WaitlistService(TermRepository terms, WaitlistRepository waitlist, Clock clock) {
        this.terms = terms;
        this.waitlist = waitlist;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<WaitlistEntry> listEntries(UserId ownerId, TermId termId) {
        Term term = terms.findByIdAndOwner(termId, ownerId).orElseThrow(WaitlistNotFoundException::new);
        return waitlist.findByTerm(term.id());
    }

    public WaitlistEntry add(
            TermId termId,
            @Nullable UserId participantUserId,
            String firstName,
            String lastName,
            String email,
            String phoneNumber
    ) {
        return waitlist.save(WaitlistEntry.create(
                termId,
                participantUserId,
                firstName,
                lastName,
                email,
                phoneNumber,
                waitlist.nextPosition(termId),
                clock.instant()
        ));
    }

    public WaitlistEntry cancelByParticipant(String cancellationToken) {
        WaitlistEntry entry = waitlist.findByCancellationToken(cancellationToken).orElseThrow(WaitlistNotFoundException::new);
        waitlist.delete(entry);
        compactPositions(entry.termId(), clock.instant());
        return entry;
    }

    public WaitlistEntry removeByOwner(UserId ownerId, TermId termId, WaitlistEntryId entryId) {
        Term term = terms.findByIdForUpdate(termId).orElseThrow(WaitlistNotFoundException::new);
        if (!term.ownerId().equals(ownerId)) {
            throw new WaitlistNotFoundException();
        }
        WaitlistEntry entry = waitlist.findByIdAndTerm(entryId, termId).orElseThrow(WaitlistNotFoundException::new);
        waitlist.delete(entry);
        compactPositions(termId, clock.instant());
        return entry;
    }

    public Optional<WaitlistEntry> pollFirst(TermId termId) {
        Optional<WaitlistEntry> first = waitlist.findFirstByTerm(termId);
        first.ifPresent(entry -> {
            waitlist.delete(entry);
            compactPositions(termId, clock.instant());
        });
        return first;
    }

    private void compactPositions(TermId termId, Instant now) {
        int position = 1;
        for (WaitlistEntry entry : waitlist.findByTerm(termId)) {
            if (entry.position() != position) {
                waitlist.save(entry.withPosition(position, now));
            }
            position++;
        }
    }
}
