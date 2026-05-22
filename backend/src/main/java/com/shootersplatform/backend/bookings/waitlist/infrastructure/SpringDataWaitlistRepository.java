package com.shootersplatform.backend.bookings.waitlist.infrastructure;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NullMarked
interface SpringDataWaitlistRepository extends JpaRepository<WaitlistEntryEntity, UUID> {

    List<WaitlistEntryEntity> findByTermIdOrderByPositionAscCreatedAtAsc(UUID termId);

    Optional<WaitlistEntryEntity> findByIdAndTermId(UUID id, UUID termId);

    Optional<WaitlistEntryEntity> findByCancellationToken(String token);

    Optional<WaitlistEntryEntity> findFirstByTermIdOrderByPositionAscCreatedAtAsc(UUID termId);

    boolean existsByTermIdAndEmail(UUID termId, String email);

    @Query("select coalesce(max(entry.position), 0) from WaitlistEntryEntity entry where entry.termId = :termId")
    @Nullable Integer findMaxPosition(UUID termId);
}
