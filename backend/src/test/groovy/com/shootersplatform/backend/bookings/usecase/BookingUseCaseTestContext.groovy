package com.shootersplatform.backend.bookings.usecase

import com.shootersplatform.backend.bookings.notification.BookingNotificationServiceTestConfiguration
import com.shootersplatform.backend.bookings.reservation.ReservationServiceTestConfiguration
import com.shootersplatform.backend.bookings.term.TermServiceTestConfiguration
import com.shootersplatform.backend.bookings.trainingtemplate.TrainingTemplateServiceTestConfiguration
import com.shootersplatform.backend.bookings.waitlist.WaitlistServiceTestConfiguration
import com.shootersplatform.backend.identity.InMemoryLoginRateLimiter
import com.shootersplatform.backend.identity.InMemoryUserAccountRepository
import com.shootersplatform.backend.identity.PlainTextPasswordHasher
import com.shootersplatform.backend.identity.domain.IdentityService
import com.shootersplatform.backend.identity.usecase.IdentityUseCaseFactory

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class BookingUseCaseTestContext {

    private static final Instant BASE_TIME = Instant.parse("2026-05-08T10:00:00Z")

    final CreateTermUseCase createTerm
    final UpdateTermUseCase updateTerm
    final GetPublicTermUseCase getPublicTerm
    final ListPublicTermsUseCase listPublicTerms
    final ListOwnerTermsUseCase listOwnerTerms

    final CreateReservationUseCase createReservation
    final CancelReservationByParticipantUseCase cancelReservationByParticipant
    final CancelReservationByInstructorUseCase cancelReservationByInstructor
    final ConfirmWaitlistOfferUseCase confirmWaitlistOffer
    final ExpireWaitlistOffersUseCase expireWaitlistOffers
    final ListReservationsUseCase listReservations

    final CancelWaitlistEntryByParticipantUseCase cancelWaitlistEntryByParticipant
    final RemoveWaitlistEntryByOwnerUseCase removeWaitlistEntryByOwner
    final ListWaitlistEntriesUseCase listWaitlistEntries

    final CreateTrainingTemplateUseCase createTrainingTemplate
    final GetTrainingTemplateUseCase getTrainingTemplate
    final UpdateTrainingTemplateUseCase updateTrainingTemplate
    final DeleteTrainingTemplateUseCase deleteTrainingTemplate
    final ListTrainingTemplatesUseCase listTrainingTemplates

    BookingUseCaseTestContext() {
        this(Clock.fixed(BASE_TIME, ZoneOffset.UTC))
    }

    BookingUseCaseTestContext(Clock clock) {
        def terms = TermServiceTestConfiguration.inMemory(clock)
        def reservations = ReservationServiceTestConfiguration.inMemory(clock)
        def waitlist = WaitlistServiceTestConfiguration.inMemory(clock)
        def notifications = BookingNotificationServiceTestConfiguration.inMemory()
        def trainingTemplates = TrainingTemplateServiceTestConfiguration.inMemory(clock)
        def waitlistPromotion = new WaitlistPromotionCoordinator(reservations, waitlist, notifications, clock)

        createTerm = new CreateTermUseCase(terms)
        updateTerm = new UpdateTermUseCase(terms, reservations)
        getPublicTerm = new GetPublicTermUseCase(terms, reservations)
        listPublicTerms = new ListPublicTermsUseCase(terms, reservations)
        listOwnerTerms = new ListOwnerTermsUseCase(terms, reservations)

        createReservation = new CreateReservationUseCase(
                reservations,
                terms,
                waitlist,
                notifications,
                registerUser(clock)
        )
        cancelReservationByParticipant = new CancelReservationByParticipantUseCase(reservations, terms, waitlistPromotion)
        cancelReservationByInstructor = new CancelReservationByInstructorUseCase(reservations, terms, waitlistPromotion)
        confirmWaitlistOffer = new ConfirmWaitlistOfferUseCase(reservations, terms)
        expireWaitlistOffers = new ExpireWaitlistOffersUseCase(reservations, terms, waitlistPromotion)
        listReservations = new ListReservationsUseCase(reservations, terms)

        cancelWaitlistEntryByParticipant = new CancelWaitlistEntryByParticipantUseCase(waitlist)
        removeWaitlistEntryByOwner = new RemoveWaitlistEntryByOwnerUseCase(waitlist, terms)
        listWaitlistEntries = new ListWaitlistEntriesUseCase(waitlist, terms)

        createTrainingTemplate = new CreateTrainingTemplateUseCase(trainingTemplates)
        getTrainingTemplate = new GetTrainingTemplateUseCase(trainingTemplates)
        updateTrainingTemplate = new UpdateTrainingTemplateUseCase(trainingTemplates)
        deleteTrainingTemplate = new DeleteTrainingTemplateUseCase(trainingTemplates)
        listTrainingTemplates = new ListTrainingTemplatesUseCase(trainingTemplates)
    }

    private static registerUser(Clock clock) {
        def identity = new IdentityService(
                new InMemoryUserAccountRepository(),
                new PlainTextPasswordHasher(),
                clock
        )
        return IdentityUseCaseFactory.registerUser(identity, new InMemoryLoginRateLimiter())
    }
}
