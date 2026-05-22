package com.shootersplatform.backend.bookings.web;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.usecase.CancelWaitlistEntryByParticipantUseCase;
import com.shootersplatform.backend.bookings.usecase.ListWaitlistEntriesUseCase;
import com.shootersplatform.backend.bookings.usecase.RemoveWaitlistEntryByOwnerUseCase;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistEntryId;
import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
class BookingWaitlistController {

    private final ListWaitlistEntriesUseCase listWaitlistEntries;
    private final CancelWaitlistEntryByParticipantUseCase cancelWaitlistEntryByParticipant;
    private final RemoveWaitlistEntryByOwnerUseCase removeWaitlistEntryByOwner;

    BookingWaitlistController(
        ListWaitlistEntriesUseCase listWaitlistEntries,
        CancelWaitlistEntryByParticipantUseCase cancelWaitlistEntryByParticipant,
        RemoveWaitlistEntryByOwnerUseCase removeWaitlistEntryByOwner
    ) {
        this.listWaitlistEntries = listWaitlistEntries;
        this.cancelWaitlistEntryByParticipant = cancelWaitlistEntryByParticipant;
        this.removeWaitlistEntryByOwner = removeWaitlistEntryByOwner;
    }

    @GetMapping("/terms/{termId}/waitlist")
    @PreAuthorize("hasRole('USER')")
    List<WaitlistEntryResponse> list(@PathVariable UUID termId, Authentication authentication) {
        return listWaitlistEntries.list(currentUser(authentication).id(), new TermId(termId)).stream()
            .map(WaitlistEntryResponse::from)
            .toList();
    }

    @PostMapping("/waitlist/cancel-by-participant")
    WaitlistEntryResponse cancelByParticipant(@Valid @RequestBody WaitlistTokenRequest request) {
        return WaitlistEntryResponse.from(cancelWaitlistEntryByParticipant.cancel(request.token()));
    }

    @PostMapping("/terms/{termId}/waitlist/{waitlistEntryId}/remove-by-owner")
    @PreAuthorize("hasRole('USER')")
    WaitlistEntryResponse removeByOwner(
        @PathVariable UUID termId,
        @PathVariable UUID waitlistEntryId,
        Authentication authentication
    ) {
        return WaitlistEntryResponse.from(removeWaitlistEntryByOwner.remove(
            currentUser(authentication).id(),
            new TermId(termId),
            new WaitlistEntryId(waitlistEntryId)
        ));
    }

    private static AuthenticatedUser currentUser(@Nullable Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return user;
    }
}
