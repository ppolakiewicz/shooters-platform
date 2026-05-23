package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.notification.domain.BookingNotification;
import com.shootersplatform.backend.bookings.notification.domain.BookingNotificationService;
import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationService;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationValidationException;
import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.term.domain.TermService;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistEntry;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistService;
import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import com.shootersplatform.backend.identity.domain.EmailAddress;
import com.shootersplatform.backend.identity.domain.UserId;
import com.shootersplatform.backend.identity.usecase.RegisterUserUseCase;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateReservationUseCase {

    private final ReservationService reservationService;
    private final TermService termService;
    private final WaitlistService waitlistService;
    private final BookingNotificationService notifications;
    private final RegisterUserUseCase registerUser;

    CreateReservationUseCase(
            ReservationService reservationService,
            TermService termService,
            WaitlistService waitlistService,
            BookingNotificationService notifications,
            RegisterUserUseCase registerUser
    ) {
        this.reservationService = reservationService;
        this.termService = termService;
        this.waitlistService = waitlistService;
        this.notifications = notifications;
        this.registerUser = registerUser;
    }

    @Transactional
    public CreateReservationResult create(
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

        Term term = termService.requireReservable(termId);
        EmailAddress emailAddress = new EmailAddress(email);
        if (reservationService.hasActiveReservation(term.id(), emailAddress) || waitlistService.hasParticipant(term.id(), emailAddress)) {
            throw new ReservationValidationException("Participant is already registered for this term");
        }

        CreatedBooking created;
        if (reservationService.countOccupiedPlaces(term.id()) < term.capacity()) {
            Reservation reservation = reservationService.createConfirmed(term.id(), participantUserId, firstName, lastName, email, phoneNumber);
            notifications.send(BookingNotification.reservationConfirmed(term, reservation));
            created = CreatedBooking.reservation(reservation);
        } else {
            WaitlistEntry entry = waitlistService.add(term.id(), participantUserId, firstName, lastName, email, phoneNumber);
            created = CreatedBooking.waitlistEntry(entry);
        }
        return new CreateReservationResult(created, registeredUser);
    }
}
