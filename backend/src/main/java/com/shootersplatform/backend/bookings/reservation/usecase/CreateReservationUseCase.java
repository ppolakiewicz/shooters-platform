package com.shootersplatform.backend.bookings.reservation.usecase;

import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationValidationException;
import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import com.shootersplatform.backend.identity.domain.UserId;
import com.shootersplatform.backend.identity.usecase.RegisterUserUseCase;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateReservationUseCase {

    private final ReservationService reservationService;
    private final RegisterUserUseCase registerUser;

    public CreateReservationUseCase(ReservationService reservationService, RegisterUserUseCase registerUser) {
        this.reservationService = reservationService;
        this.registerUser = registerUser;
    }

    @Transactional
    public CreatedReservation create(
            TermId termId,
            @Nullable AuthenticatedUser currentUser,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            boolean createAccount,
            @Nullable String username,
            @Nullable String password,
            String clientIp
    ) {
        if (currentUser != null && createAccount) {
            throw new ReservationValidationException("Authenticated users cannot create another account while reserving");
        }

        AuthenticatedUser registeredUser = null;
        UserId participantUserId = currentUser == null ? null : currentUser.id();
        if (currentUser == null && createAccount) {
            if (username == null || password == null) {
                throw new ReservationValidationException("Username and password are required to create an account");
            }
            registeredUser = registerUser.register(email, username, password, clientIp);
            participantUserId = registeredUser.id();
        }

        Reservation reservation = reservationService.createReservation(termId, participantUserId, firstName, lastName, email, phoneNumber);
        return new CreatedReservation(reservation, registeredUser);
    }
}
