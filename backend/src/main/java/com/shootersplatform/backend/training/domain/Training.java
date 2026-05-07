package com.shootersplatform.backend.training.domain;

import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public record Training(
        TrainingId id,
        UserId ownerId,
        String name,
        String place,
        String description,
        LocalDate performedOn,
        WeaponType weaponType,
        ScoringType scoringType,
        List<ShootingTask> tasks,
        Instant createdAt,
        Instant updatedAt
) {

    private static final int TEXT_MAX_LENGTH = 120;
    private static final int DESCRIPTION_MAX_LENGTH = 2048;

    public static Training create(
            UserId ownerId,
            String name,
            String place,
            String description,
            LocalDate performedOn,
            WeaponType weaponType,
            ScoringType scoringType,
            Instant now
    ) {
        return new Training(TrainingId.newId(), ownerId, name, place, description, performedOn, weaponType, scoringType, List.of(), now, now);
    }

    public Training {
        validateBasics(name, place, description, performedOn, weaponType, scoringType);

        name = normalizeRequiredText(name, "Training name");
        place = normalizeRequiredText(place, "Training place");
        description = normalizeOptionalText(description, "Training description", DESCRIPTION_MAX_LENGTH);
        tasks = tasks.stream()
                .sorted(Comparator.comparingInt(ShootingTask::runNumber))
                .toList();
    }

    public static void validateBasics(
            @Nullable String name,
            @Nullable String place,
            @Nullable String description,
            @Nullable LocalDate performedOn,
            @Nullable WeaponType weaponType,
            @Nullable ScoringType scoringType
    ) {
        normalizeRequiredText(name, "Training name");
        normalizeRequiredText(place, "Training place");
        normalizeOptionalText(description, "Training description", DESCRIPTION_MAX_LENGTH);
        if (performedOn == null) {
            throw new TrainingValidationException("Training date is required");
        }
        if (weaponType == null) {
            throw new TrainingValidationException("Weapon type is required");
        }
        if (scoringType == null) {
            throw new TrainingValidationException("Scoring type is required");
        }
    }

    public Training update(
            String updatedName,
            String updatedPlace,
            String updatedDescription,
            LocalDate updatedPerformedOn,
            WeaponType updatedWeaponType,
            ScoringType updatedScoringType,
            Instant now
    ) {
        return new Training(id, ownerId, updatedName, updatedPlace, updatedDescription, updatedPerformedOn, updatedWeaponType, updatedScoringType, tasks, createdAt, now);
    }

    public Training addTask(ShootingTask task, Instant now) {
        return new Training(id, ownerId, name, place, description, performedOn, weaponType, scoringType, appendTask(task), createdAt, now);
    }

    public Training updateTask(ShootingTaskId taskId, WeaponType updatedWeaponType, HitScore updatedScore, int updatedDurationTenths, Instant now) {
        boolean[] found = {false};
        List<ShootingTask> updatedTasks = tasks.stream()
                .map(task -> {
                    if (!task.id().equals(taskId)) {
                        return task;
                    }
                    found[0] = true;
                    return task.update(updatedWeaponType, updatedScore, updatedDurationTenths);
                })
                .toList();

        if (!found[0]) {
            throw new TrainingNotFoundException();
        }

        return new Training(id, ownerId, name, place, description, performedOn, weaponType, scoringType, updatedTasks, createdAt, now);
    }

    public Training removeTask(ShootingTaskId taskId, Instant now) {
        List<ShootingTask> updatedTasks = tasks.stream()
                .filter(task -> !task.id().equals(taskId))
                .toList();

        if (updatedTasks.size() == tasks.size()) {
            throw new TrainingNotFoundException();
        }

        return new Training(id, ownerId, name, place, description, performedOn, weaponType, scoringType, updatedTasks, createdAt, now);
    }

    public int nextRunNumber() {
        return tasks.stream()
                .mapToInt(ShootingTask::runNumber)
                .max()
                .orElse(0) + 1;
    }

    public TrainingSummary toSummary() {
        return new TrainingSummary(id, ownerId, name, place, description, performedOn, weaponType, scoringType, tasks.size(), createdAt, updatedAt);
    }

    private List<ShootingTask> appendTask(ShootingTask task) {
        if (tasks.stream().anyMatch(existing -> existing.runNumber() == task.runNumber())) {
            throw new TrainingValidationException("Run number must be unique within a training");
        }
        return java.util.stream.Stream.concat(tasks.stream(), java.util.stream.Stream.of(task)).toList();
    }

    private static String normalizeRequiredText(@Nullable String value, String fieldName) {
        if (value == null) {
            throw new TrainingValidationException(fieldName + " is required");
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new TrainingValidationException(fieldName + " is required");
        }
        if (normalized.length() > TEXT_MAX_LENGTH) {
            throw new TrainingValidationException(fieldName + " cannot exceed " + TEXT_MAX_LENGTH + " characters");
        }
        return normalized;
    }

    private static String normalizeOptionalText(@Nullable String value, String fieldName, int maxLength) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new TrainingValidationException(fieldName + " cannot exceed " + maxLength + " characters");
        }
        return normalized;
    }
}
