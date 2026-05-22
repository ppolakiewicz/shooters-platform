package com.shootersplatform.backend.bookings.reservation.infrastructure;

import com.shootersplatform.backend.bookings.reservation.domain.ReservationStatus;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NullMarked
interface SpringDataReservationRepository extends JpaRepository<ReservationEntity, UUID> {

    List<ReservationEntity> findByTermIdOrderByCreatedAtAsc(UUID termId);

    Optional<ReservationEntity> findByIdAndTermId(UUID id, UUID termId);

    Optional<ReservationEntity> findByCancellationToken(String token);

    Optional<ReservationEntity> findByWaitlistConfirmationToken(String token);

    List<ReservationEntity> findByStatusAndWaitlistOfferExpiresAtLessThanEqual(ReservationStatus status, Instant now);

    boolean existsByTermIdAndEmailAndStatusIn(UUID termId, String email, Collection<ReservationStatus> statuses);

    long countByTermIdAndStatusIn(UUID termId, Collection<ReservationStatus> statuses);

}
