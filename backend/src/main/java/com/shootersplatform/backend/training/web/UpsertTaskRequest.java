package com.shootersplatform.backend.training.web;

import com.shootersplatform.backend.training.domain.HitScore;
import com.shootersplatform.backend.training.domain.ScoringType;
import com.shootersplatform.backend.training.domain.WeaponType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Map;

record UpsertTaskRequest(
        @NotNull WeaponType weaponType,
        @NotNull ScoringType scoringType,
        @Positive int durationTenths,
        @NotNull Map<String, Integer> score
) {

    HitScore toDomainScore() {
        return new HitScore(scoringType, score);
    }
}
