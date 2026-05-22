package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistEntry;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistEntryId;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistService;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveWaitlistEntryByOwnerUseCase {

    private final WaitlistService waitlist;

    public RemoveWaitlistEntryByOwnerUseCase(WaitlistService waitlist) {
        this.waitlist = waitlist;
    }

    @Transactional
    public WaitlistEntry remove(UserId ownerId, TermId termId, WaitlistEntryId entryId) {
        return waitlist.removeByOwner(ownerId, termId, entryId);
    }
}
