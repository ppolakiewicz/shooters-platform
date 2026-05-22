package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistEntry;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelWaitlistEntryByParticipantUseCase {

    private final WaitlistService waitlist;

    public CancelWaitlistEntryByParticipantUseCase(WaitlistService waitlist) {
        this.waitlist = waitlist;
    }

    @Transactional
    public WaitlistEntry cancel(String cancellationToken) {
        return waitlist.cancelByParticipant(cancellationToken);
    }
}
