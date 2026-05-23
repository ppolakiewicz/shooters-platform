package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.term.domain.TermService;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistEntry;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistEntryId;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistService;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveWaitlistEntryByOwnerUseCase {

    private final WaitlistService waitlist;
    private final TermService terms;

    RemoveWaitlistEntryByOwnerUseCase(WaitlistService waitlist, TermService terms) {
        this.waitlist = waitlist;
        this.terms = terms;
    }

    @Transactional
    public WaitlistEntry remove(UserId ownerId, TermId termId, WaitlistEntryId entryId) {
        terms.requireOwnedForUpdate(ownerId, termId);
        return waitlist.remove(termId, entryId);
    }
}
