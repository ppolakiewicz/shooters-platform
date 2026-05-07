package com.shootersplatform.backend.training.infrastructure;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataTrainingRepository extends JpaRepository<TrainingEntity, UUID> {

    List<TrainingEntity> findByOwnerUserIdOrderByPerformedOnDescCreatedAtDesc(UUID ownerUserId);

    @EntityGraph(attributePaths = "tasks")
    Optional<TrainingEntity> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);
}
