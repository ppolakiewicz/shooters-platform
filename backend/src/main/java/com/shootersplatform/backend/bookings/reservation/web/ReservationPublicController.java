package com.shootersplatform.backend.bookings.reservation.web;

import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.reservation.usecase.CreateReservationUseCase;
import com.shootersplatform.backend.bookings.reservation.usecase.CreatedBookingResult;
import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import com.shootersplatform.backend.identity.web.SecuritySessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings/reservations")
class ReservationPublicController {

    private final ReservationService reservationService;
    private final CreateReservationUseCase createReservation;
    private final SecuritySessionService sessions;

    ReservationPublicController(
        ReservationService reservationService,
        CreateReservationUseCase createReservation,
        SecuritySessionService sessions) {

        this.reservationService = reservationService;
        this.createReservation = createReservation;
        this.sessions = sessions;
    }

    @PostMapping("/reserve")
    @ResponseStatus(HttpStatus.CREATED)
    CreatedBookingResponse reserve(
            @Valid @RequestBody CreateReservationRequest request,
            @Nullable Authentication authentication,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        CreatedBookingResult result = createReservation.create(
                new TermId(request.termId()),
                currentUser(authentication),
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

    @PostMapping("/confirm-waitlist-offer")
    ReservationResponse confirmWaitlist(@Valid @RequestBody ReservationTokenRequest request) {
        return ReservationResponse.from(reservationService.confirmWaitlistOffer(request.token()));
    }

    @PostMapping("/cancel-by-participant")
    ReservationResponse cancel(@Valid @RequestBody ReservationTokenRequest request) {
        return ReservationResponse.from(reservationService.cancelByParticipant(request.token()));
    }

    @Nullable
    private static AuthenticatedUser currentUser(@Nullable Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return null;
        }
        return user;
    }
}
