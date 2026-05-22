package com.shootersplatform.backend.bookings.web;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.usecase.*;
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
class BookingTermsController {

    private final ListPublicTermsUseCase listPublicTerms;
    private final GetPublicTermUseCase getPublicTerm;
    private final ListOwnerTermsUseCase listOwnerTerms;
    private final CreateTermUseCase createTerm;
    private final UpdateTermUseCase updateTerm;

    BookingTermsController(
        ListPublicTermsUseCase listPublicTerms,
        GetPublicTermUseCase getPublicTerm,
        ListOwnerTermsUseCase listOwnerTerms,
        CreateTermUseCase createTerm,
        UpdateTermUseCase updateTerm
    ) {
        this.listPublicTerms = listPublicTerms;
        this.getPublicTerm = getPublicTerm;
        this.listOwnerTerms = listOwnerTerms;
        this.createTerm = createTerm;
        this.updateTerm = updateTerm;
    }

    @GetMapping("/public/terms")
    List<TermResponse> listPublic() {
        return listPublicTerms.list().stream().map(TermResponse::from).toList();
    }

    @GetMapping("/public/terms/{termId}")
    TermResponse getPublic(@PathVariable UUID termId) {
        return TermResponse.from(getPublicTerm.get(new TermId(termId)));
    }

    @GetMapping("/terms")
    @PreAuthorize("hasRole('USER')")
    List<TermResponse> listOwner(Authentication authentication) {
        return listOwnerTerms.list(currentUser(authentication).id()).stream().map(TermResponse::from).toList();
    }

    @PostMapping("/terms")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    TermResponse create(@Valid @RequestBody CreateTermRequest request, Authentication authentication) {
        return TermResponse.from(createTerm.create(
                currentUser(authentication).id(),
                request.name(),
                request.description(),
                request.location().toDomain(),
                request.capacity(),
                request.cancellationDeadlineDays(),
                request.durationMinutes(),
                request.startsAt()
        ));
    }

    @PutMapping("/terms/{termId}")
    @PreAuthorize("hasRole('USER')")
    TermResponse update(@PathVariable UUID termId, @Valid @RequestBody UpdateTermRequest request, Authentication authentication) {
        return TermResponse.from(updateTerm.update(
                currentUser(authentication).id(),
                new TermId(termId),
                request.name(),
                request.description(),
                request.location().toDomain(),
                request.cancellationDeadlineDays(),
                request.durationMinutes(),
                request.startsAt()
        ));
    }

    private static AuthenticatedUser currentUser(@Nullable Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return user;
    }
}
