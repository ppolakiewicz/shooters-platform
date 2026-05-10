package com.shootersplatform.backend.bookings.term.web;

import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.term.domain.TermService;
import com.shootersplatform.backend.bookings.term.usecase.TermAvailabilityUseCase;
import com.shootersplatform.backend.bookings.term.usecase.UpdateTermUseCase;
import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings/terms")
@PreAuthorize("hasRole('USER')")
class TermManagementController {

    private final TermService terms;
    private final UpdateTermUseCase updateTerm;
    private final TermAvailabilityUseCase termAvailability;

    TermManagementController(TermService terms, UpdateTermUseCase updateTerm, TermAvailabilityUseCase termAvailability) {
        this.terms = terms;
        this.updateTerm = updateTerm;
        this.termAvailability = termAvailability;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TermResponse create(@Valid @RequestBody UpsertTermRequest request, Authentication authentication) {
        return toResponse(terms.create(
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

    @GetMapping
    List<TermResponse> list(Authentication authentication) {
        return termAvailability.listOwner(currentUser(authentication).id()).stream().map(TermResponse::from).toList();
    }

    @PutMapping("/{termId}")
    TermResponse update(@PathVariable UUID termId, @Valid @RequestBody UpsertTermRequest request, Authentication authentication) {
        return toResponse(updateTerm.update(
                currentUser(authentication).id(),
                new TermId(termId),
                request.name(),
                request.description(),
                request.location().toDomain(),
                request.capacity(),
                request.cancellationDeadlineDays(),
                request.durationMinutes(),
                request.startsAt()
        ));
    }

    private TermResponse toResponse(Term term) {
        return TermResponse.from(termAvailability.withAvailability(term));
    }

    private static AuthenticatedUser currentUser(@Nullable Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return user;
    }
}
