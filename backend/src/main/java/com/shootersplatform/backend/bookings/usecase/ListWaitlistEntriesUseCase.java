package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistEntry;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistService;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListWaitlistEntriesUseCase {

    private final WaitlistService waitlist;

    public ListWaitlistEntriesUseCase(WaitlistService waitlist) {
        this.waitlist = waitlist;
    }

    @Transactional(readOnly = true)
    public List<WaitlistEntry> list(UserId ownerId, TermId termId) {
        return waitlist.listEntries(ownerId, termId);
    }
}
