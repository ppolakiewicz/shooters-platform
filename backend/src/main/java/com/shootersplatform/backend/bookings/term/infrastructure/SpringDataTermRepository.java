package com.shootersplatform.backend.bookings.term.infrastructure;

import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NullMarked
interface SpringDataTermRepository extends JpaRepository<TermEntity, UUID> {

    List<TermEntity> findAllByOrderByStartsAtAscCreatedAtDesc();

    List<TermEntity> findByOwnerUserIdOrderByStartsAtAsc(UUID ownerUserId);

    Optional<TermEntity> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select term from TermEntity term where term.id = :id")
    Optional<TermEntity> findByIdForUpdate(UUID id);
}
