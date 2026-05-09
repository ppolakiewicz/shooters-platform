package com.shootersplatform.backend.bookings.reservation.web;

import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationId;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@RestController
@RequestMapping("/api/bookings/reservations")
@PreAuthorize("hasRole('USER')")
class ReservationManagementController {

    private final ReservationService bookings;

    ReservationManagementController(ReservationService bookings) {
        this.bookings = bookings;
    }

    @PostMapping("/list")
    List<ReservationResponse> listReservations(@Valid @RequestBody ListReservationsRequest request, Authentication authentication) {
        return bookings.listReservations(currentUser(authentication).id(), new TermId(request.termId())).stream().map(ReservationResponse::from).toList();
    }

    @PostMapping("/cancel-by-instructor")
    ReservationResponse cancelReservation(@Valid @RequestBody CancelReservationRequest request, Authentication authentication) {
        return ReservationResponse.from(bookings.cancelByInstructor(
                currentUser(authentication).id(),
                new TermId(request.termId()),
                new ReservationId(request.reservationId())
        ));
    }

    @PostMapping("/expire-waitlist-offers")
    ExpiredOffersResponse expireOffers(@Valid @RequestBody ExpireWaitlistOffersRequest request, Authentication authentication) {
        return new ExpiredOffersResponse(bookings.expireWaitlistOffers(currentUser(authentication).id(), new TermId(request.termId())));
    }

    private static AuthenticatedUser currentUser(@Nullable Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return user;
    }
}
