package com.shootersplatform.backend.bookings.waitlist.domain;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.identity.domain.EmailAddress;
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

    private final WaitlistRepository waitlist;
    private final Clock clock;

    public WaitlistService(WaitlistRepository waitlist, Clock clock) {
        this.waitlist = waitlist;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<WaitlistEntry> listEntries(TermId termId) {
        return waitlist.findByTerm(termId);
    }

    @Transactional(readOnly = true)
    public boolean hasParticipant(TermId termId, EmailAddress email) {
        return waitlist.existsByTermAndEmail(termId, email);
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

    public WaitlistEntry remove(TermId termId, WaitlistEntryId entryId) {
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
