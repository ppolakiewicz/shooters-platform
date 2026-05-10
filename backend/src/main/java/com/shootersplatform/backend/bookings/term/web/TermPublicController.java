package com.shootersplatform.backend.bookings.term.web;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.term.usecase.TermAvailabilityUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings/public/terms")
class TermPublicController {

    private final TermAvailabilityUseCase termAvailability;

    TermPublicController(TermAvailabilityUseCase termAvailability) {
        this.termAvailability = termAvailability;
    }

    @GetMapping
    List<TermResponse> list() {
        return termAvailability.listPublic().stream().map(TermResponse::from).toList();
    }

    @GetMapping("/{termId}")
    TermResponse get(@PathVariable UUID termId) {
        return TermResponse.from(termAvailability.getPublic(new TermId(termId)));
    }
}
