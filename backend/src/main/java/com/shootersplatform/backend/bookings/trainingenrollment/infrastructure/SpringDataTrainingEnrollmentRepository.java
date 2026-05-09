package com.shootersplatform.backend.bookings.trainingenrollment.infrastructure;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NullMarked
interface SpringDataTrainingEnrollmentRepository extends JpaRepository<TrainingEnrollmentEntity, UUID> {

    List<TrainingEnrollmentEntity> findByOwnerUserIdOrderByCreatedAtDesc(UUID ownerUserId);

    Optional<TrainingEnrollmentEntity> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);
}
