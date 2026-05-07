package com.shootersplatform.backend.training.domain;

public record ShootingTask(
        ShootingTaskId id,
        int runNumber,
        WeaponType weaponType,
        HitScore score,
        int durationTenths
) {

    public static ShootingTask create(
            int runNumber,
            WeaponType weaponType,
            HitScore score,
            int durationTenths
    ) {
        return new ShootingTask(ShootingTaskId.newId(), runNumber, weaponType, score, durationTenths);
    }

    public ShootingTask {
        if (runNumber < 1) {
            throw new TrainingValidationException("Run number must be positive");
        }
        if (durationTenths < 1) {
            throw new TrainingValidationException("Duration must be greater than zero");
        }
        if (!score.hasAnyHit()) {
            throw new TrainingValidationException("Score requires at least one hit or miss");
        }
    }

    public ScoringType scoringType() {
        return score.type();
    }

    public ShootingTask update(WeaponType updatedWeaponType, HitScore updatedScore, int updatedDurationTenths) {
        return new ShootingTask(id, runNumber, updatedWeaponType, updatedScore, updatedDurationTenths);
    }
}
