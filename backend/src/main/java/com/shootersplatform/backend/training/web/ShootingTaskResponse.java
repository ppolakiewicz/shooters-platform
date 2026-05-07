package com.shootersplatform.backend.training.web;

import com.shootersplatform.backend.training.domain.ScoringType;
import com.shootersplatform.backend.training.domain.ShootingTask;
import com.shootersplatform.backend.training.domain.WeaponType;

import java.util.Map;
import java.util.UUID;

record ShootingTaskResponse(
        UUID id,
        int runNumber,
        WeaponType weaponType,
        ScoringType scoringType,
        int durationTenths,
        Map<String, Integer> score
) {

    static ShootingTaskResponse from(ShootingTask task) {
        return new ShootingTaskResponse(
                task.id().value(),
                task.runNumber(),
                task.weaponType(),
                task.scoringType(),
                task.durationTenths(),
                task.score().hits()
        );
    }
}
