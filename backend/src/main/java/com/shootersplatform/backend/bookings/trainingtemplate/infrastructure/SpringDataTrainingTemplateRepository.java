package com.shootersplatform.backend.bookings.trainingtemplate.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataTrainingTemplateRepository extends JpaRepository<TrainingTemplateEntity, UUID> {

    List<TrainingTemplateEntity> findByOwnerUserIdOrderByUpdatedAtDesc(UUID ownerUserId);

    Optional<TrainingTemplateEntity> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);
}
