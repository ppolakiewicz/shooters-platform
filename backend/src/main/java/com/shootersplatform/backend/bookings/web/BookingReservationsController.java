package com.shootersplatform.backend.bookings.web;

import com.shootersplatform.backend.bookings.reservation.domain.ReservationId;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.usecase.*;
import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import com.shootersplatform.backend.identity.web.SecuritySessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
class BookingReservationsController {

    private final CreateReservationUseCase createReservation;
    private final ListReservationsUseCase listReservations;
    private final CancelReservationByParticipantUseCase cancelReservationByParticipant;
    private final CancelReservationByInstructorUseCase cancelReservationByInstructor;
    private final ConfirmWaitlistOfferUseCase confirmWaitlistOffer;
    private final ExpireWaitlistOffersUseCase expireWaitlistOffers;
    private final SecuritySessionService sessions;

    BookingReservationsController(
        CreateReservationUseCase createReservation,
        ListReservationsUseCase listReservations,
        CancelReservationByParticipantUseCase cancelReservationByParticipant,
        CancelReservationByInstructorUseCase cancelReservationByInstructor,
        ConfirmWaitlistOfferUseCase confirmWaitlistOffer,
        ExpireWaitlistOffersUseCase expireWaitlistOffers,
        SecuritySessionService sessions
    ) {
        this.createReservation = createReservation;
        this.listReservations = listReservations;
        this.cancelReservationByParticipant = cancelReservationByParticipant;
        this.cancelReservationByInstructor = cancelReservationByInstructor;
        this.confirmWaitlistOffer = confirmWaitlistOffer;
        this.expireWaitlistOffers = expireWaitlistOffers;
        this.sessions = sessions;
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    CreatedBookingResponse create(
        @Valid @RequestBody CreateReservationRequest request,
        @Nullable Authentication authentication,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    ) {
        CreateReservationResult result = createReservation.create(
            new TermId(request.termId()),
            currentUserOrNull(authentication),
            request.firstName(),
            request.lastName(),
            request.email(),
            request.phoneNumber(),
            request.createAccount(),
            request.username(),
            request.password(),
            servletRequest.getRemoteAddr()
        );
        if (result.registeredUser() != null) {
            sessions.authenticate(result.registeredUser(), servletRequest, servletResponse);
        }
        return CreatedBookingResponse.from(result.booking());
    }

    @GetMapping("/terms/{termId}/reservations")
    @PreAuthorize("hasRole('USER')")
    List<ReservationResponse> list(@PathVariable UUID termId, Authentication authentication) {
        return listReservations.list(currentUser(authentication).id(), new TermId(termId)).stream()
            .map(ReservationResponse::from)
            .toList();
    }

    @PostMapping("/reservations/cancel-by-participant")
    ReservationResponse cancelByParticipant(@Valid @RequestBody ReservationTokenRequest request) {
        return ReservationResponse.from(cancelReservationByParticipant.cancel(request.token()));
    }

    @PostMapping("/terms/{termId}/reservations/{reservationId}/cancel-by-instructor")
    @PreAuthorize("hasRole('USER')")
    ReservationResponse cancelByInstructor(
        @PathVariable UUID termId,
        @PathVariable UUID reservationId,
        Authentication authentication
    ) {
        return ReservationResponse.from(cancelReservationByInstructor.cancel(
            currentUser(authentication).id(),
            new TermId(termId),
            new ReservationId(reservationId)
        ));
    }

    @PostMapping("/reservations/confirm-waitlist-offer")
    ReservationResponse confirmWaitlistOffer(@Valid @RequestBody ReservationTokenRequest request) {
        return ReservationResponse.from(confirmWaitlistOffer.confirm(request.token()));
    }

    @PostMapping("/terms/{termId}/reservations/expire-waitlist-offers")
    @PreAuthorize("hasRole('USER')")
    ExpiredOffersResponse expireWaitlistOffers(@PathVariable UUID termId, Authentication authentication) {
        return new ExpiredOffersResponse(expireWaitlistOffers.expire(currentUser(authentication).id(), new TermId(termId)));
    }

    @Nullable
    private static AuthenticatedUser currentUserOrNull(@Nullable Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return null;
        }
        return user;
    }

    private static AuthenticatedUser currentUser(@Nullable Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return user;
    }
}
