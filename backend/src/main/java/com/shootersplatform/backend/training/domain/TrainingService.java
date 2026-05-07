package com.shootersplatform.backend.training.domain;

import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TrainingService {

    private final TrainingRepository trainings;
    private final Clock clock;

    public TrainingService(TrainingRepository trainings, Clock clock) {
        this.trainings = trainings;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<TrainingSummary> list(UserId ownerId) {
        return trainings.findSummariesByOwner(ownerId);
    }

    @Transactional(readOnly = true)
    public Training get(UserId ownerId, TrainingId trainingId) {
        return findOwnedTraining(ownerId, trainingId);
    }

    public Training create(
            UserId ownerId,
            String name,
            String place,
            String description,
            LocalDate performedOn,
            WeaponType weaponType,
            ScoringType scoringType
    ) {
        return trainings.save(Training.create(ownerId, name, place, description, performedOn, weaponType, scoringType, clock.instant()));
    }

    public Training update(
            UserId ownerId,
            TrainingId trainingId,
            String name,
            String place,
            String description,
            LocalDate performedOn,
            WeaponType weaponType,
            ScoringType scoringType
    ) {
        Training training = findOwnedTraining(ownerId, trainingId);
        return trainings.save(training.update(name, place, description, performedOn, weaponType, scoringType, clock.instant()));
    }

    public void delete(UserId ownerId, TrainingId trainingId) {
        trainings.delete(findOwnedTraining(ownerId, trainingId));
    }

    public Training addTask(
            UserId ownerId,
            TrainingId trainingId,
            WeaponType weaponType,
            HitScore score,
            int durationTenths
    ) {
        Training training = findOwnedTraining(ownerId, trainingId);
        ShootingTask task = ShootingTask.create(training.nextRunNumber(), weaponType, score, durationTenths);
        return trainings.save(training.addTask(task, clock.instant()));
    }

    public Training updateTask(
            UserId ownerId,
            TrainingId trainingId,
            ShootingTaskId taskId,
            WeaponType weaponType,
            HitScore score,
            int durationTenths
    ) {
        Training training = findOwnedTraining(ownerId, trainingId);
        return trainings.save(training.updateTask(taskId, weaponType, score, durationTenths, clock.instant()));
    }

    public Training deleteTask(UserId ownerId, TrainingId trainingId, ShootingTaskId taskId) {
        Training training = findOwnedTraining(ownerId, trainingId);
        return trainings.save(training.removeTask(taskId, clock.instant()));
    }

    private Training findOwnedTraining(UserId ownerId, TrainingId trainingId) {
        return trainings.findByIdAndOwner(trainingId, ownerId).orElseThrow(TrainingNotFoundException::new);
    }
}
