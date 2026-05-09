package com.shootersplatform.backend.bookings.term.web;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.term.domain.TermService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings/public/terms")
class TermPublicController {

    private final TermService terms;

    TermPublicController(TermService terms) {
        this.terms = terms;
    }

    @GetMapping
    List<TermResponse> list() {
        return terms.listPublic().stream().map(TermResponse::from).toList();
    }

    @GetMapping("/{termId}")
    TermResponse get(@PathVariable UUID termId) {
        return TermResponse.from(terms.getPublic(new TermId(termId)));
    }
}
