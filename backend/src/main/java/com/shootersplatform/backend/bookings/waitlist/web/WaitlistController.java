package com.shootersplatform.backend.bookings.waitlist.web;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistEntryId;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistService;
import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/bookings/waitlist")
class WaitlistController {

    private final WaitlistService waitlist;

    WaitlistController(WaitlistService waitlist) {
        this.waitlist = waitlist;
    }

    @PostMapping("/list")
    @PreAuthorize("hasRole('USER')")
    List<WaitlistEntryResponse> list(@Valid @RequestBody ListWaitlistEntriesRequest request, Authentication authentication) {
        return waitlist.listEntries(currentUser(authentication).id(), new TermId(request.termId())).stream().map(WaitlistEntryResponse::from).toList();
    }

    @PostMapping("/cancel-by-participant")
    WaitlistEntryResponse cancelByParticipant(@Valid @RequestBody WaitlistTokenRequest request) {
        return WaitlistEntryResponse.from(waitlist.cancelByParticipant(request.token()));
    }

    @PostMapping("/remove-by-owner")
    @PreAuthorize("hasRole('USER')")
    WaitlistEntryResponse removeByOwner(@Valid @RequestBody RemoveWaitlistEntryRequest request, Authentication authentication) {
        return WaitlistEntryResponse.from(waitlist.removeByOwner(
                currentUser(authentication).id(),
                new TermId(request.termId()),
                new WaitlistEntryId(request.waitlistEntryId())
        ));
    }

    private static AuthenticatedUser currentUser(@Nullable Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return user;
    }
}
