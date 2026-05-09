package com.shootersplatform.backend.bookings.reservation.infrastructure;

import com.shootersplatform.backend.bookings.reservation.domain.Reservation;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationId;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationRepository;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationStatus;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.identity.domain.EmailAddress;
import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@NullMarked
@Repository
class JpaReservationRepository implements ReservationRepository {

    private static final List<ReservationStatus> ACTIVE_STATUSES = List.of(
            ReservationStatus.CONFIRMED,
            ReservationStatus.WAITLISTED,
            ReservationStatus.WAITLIST_OFFERED
    );
    private static final List<ReservationStatus> OCCUPIED_STATUSES = List.of(
            ReservationStatus.CONFIRMED,
            ReservationStatus.WAITLIST_OFFERED
    );

    private final SpringDataReservationRepository repository;

    JpaReservationRepository(SpringDataReservationRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Reservation> findByTerm(TermId termId) {
        return repository.findByTermIdOrderByCreatedAtAsc(termId.value()).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Reservation> findByIdAndTerm(ReservationId reservationId, TermId termId) {
        return repository.findByIdAndTermId(reservationId.value(), termId.value()).map(this::toDomain);
    }

    @Override
    public Optional<Reservation> findByCancellationToken(String token) {
        return repository.findByCancellationToken(token).map(this::toDomain);
    }

    @Override
    public Optional<Reservation> findByWaitlistConfirmationToken(String token) {
        return repository.findByWaitlistConfirmationToken(token).map(this::toDomain);
    }

    @Override
    public Optional<Reservation> findFirstWaitlisted(TermId termId) {
        return repository.findFirstByTermIdAndStatusOrderByWaitlistPositionAscCreatedAtAsc(termId.value(), ReservationStatus.WAITLISTED).map(this::toDomain);
    }

    @Override
    public List<Reservation> findExpiredWaitlistOffers(Instant now) {
        return repository.findByStatusAndWaitlistOfferExpiresAtLessThanEqual(ReservationStatus.WAITLIST_OFFERED, now).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsActiveByTermAndEmail(TermId termId, EmailAddress email) {
        return repository.existsByTermIdAndEmailAndStatusIn(termId.value(), email.value(), ACTIVE_STATUSES);
    }

    @Override
    public long countOccupiedPlaces(TermId termId) {
        return repository.countByTermIdAndStatusIn(termId.value(), OCCUPIED_STATUSES);
    }

    @Override
    public int nextWaitlistPosition(TermId termId) {
        Integer maxPosition = repository.findMaxWaitlistPosition(termId.value());
        return (maxPosition == null ? 0 : maxPosition) + 1;
    }

    @Override
    public Reservation save(Reservation reservation) {
        return toDomain(repository.save(toEntity(reservation)));
    }

    private Reservation toDomain(ReservationEntity entity) {
        return new Reservation(
                new ReservationId(entity.getId()),
                new TermId(entity.getTermId()),
                entity.getParticipantUserId() == null ? null : new UserId(entity.getParticipantUserId()),
                entity.getFirstName(),
                entity.getLastName(),
                new EmailAddress(entity.getEmail()),
                entity.getPhoneNumber(),
                entity.getStatus(),
                entity.getWaitlistPosition(),
                entity.getCancellationToken(),
                entity.getWaitlistConfirmationToken(),
                entity.getWaitlistOfferExpiresAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ReservationEntity toEntity(Reservation reservation) {
        ReservationEntity entity = new ReservationEntity();
        entity.setId(reservation.id().value());
        entity.setTermId(reservation.termId().value());
        entity.setParticipantUserId(reservation.participantUserId() == null ? null : reservation.participantUserId().value());
        entity.setFirstName(reservation.firstName());
        entity.setLastName(reservation.lastName());
        entity.setEmail(reservation.email().value());
        entity.setPhoneNumber(reservation.phoneNumber());
        entity.setStatus(reservation.status());
        entity.setWaitlistPosition(reservation.waitlistPosition());
        entity.setCancellationToken(reservation.cancellationToken());
        entity.setWaitlistConfirmationToken(reservation.waitlistConfirmationToken());
        entity.setWaitlistOfferExpiresAt(reservation.waitlistOfferExpiresAt());
        entity.setCreatedAt(reservation.createdAt());
        entity.setUpdatedAt(reservation.updatedAt());
        return entity;
    }
}
