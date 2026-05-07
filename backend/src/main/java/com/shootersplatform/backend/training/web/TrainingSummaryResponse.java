package com.shootersplatform.backend.training.web;

import com.shootersplatform.backend.training.domain.ScoringType;
import com.shootersplatform.backend.training.domain.TrainingSummary;
import com.shootersplatform.backend.training.domain.WeaponType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

record TrainingSummaryResponse(
        UUID id,
        String name,
        String place,
        String description,
        LocalDate performedOn,
        WeaponType weaponType,
        ScoringType scoringType,
        int taskCount,
        Instant createdAt,
        Instant updatedAt
) {

    static TrainingSummaryResponse from(TrainingSummary training) {
        return new TrainingSummaryResponse(
                training.id().value(),
                training.name(),
                training.place(),
                training.description(),
                training.performedOn(),
                training.weaponType(),
                training.scoringType(),
                training.taskCount(),
                training.createdAt(),
                training.updatedAt()
        );
    }
}
