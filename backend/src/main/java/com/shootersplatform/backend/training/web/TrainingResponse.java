package com.shootersplatform.backend.training.web;

import com.shootersplatform.backend.training.domain.ScoringType;
import com.shootersplatform.backend.training.domain.Training;
import com.shootersplatform.backend.training.domain.WeaponType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

record TrainingResponse(
        UUID id,
        String name,
        String place,
        String description,
        LocalDate performedOn,
        WeaponType weaponType,
        ScoringType scoringType,
        List<ShootingTaskResponse> tasks,
        Instant createdAt,
        Instant updatedAt
) {

    static TrainingResponse from(Training training) {
        return new TrainingResponse(
                training.id().value(),
                training.name(),
                training.place(),
                training.description(),
                training.performedOn(),
                training.weaponType(),
                training.scoringType(),
                training.tasks().stream().map(ShootingTaskResponse::from).toList(),
                training.createdAt(),
                training.updatedAt()
        );
    }
}
