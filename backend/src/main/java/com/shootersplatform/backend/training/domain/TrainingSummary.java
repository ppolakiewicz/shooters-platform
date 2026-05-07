package com.shootersplatform.backend.training.domain;

import com.shootersplatform.backend.identity.domain.UserId;

import java.time.Instant;
import java.time.LocalDate;

public record TrainingSummary(
        TrainingId id,
        UserId ownerId,
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

    public TrainingSummary {
        Training.validateBasics(name, place, description, performedOn, weaponType, scoringType);
    }
}
