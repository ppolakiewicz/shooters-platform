package com.shootersplatform.backend.training.infrastructure;

import com.shootersplatform.backend.identity.domain.UserId;
import com.shootersplatform.backend.training.domain.HitScore;
import com.shootersplatform.backend.training.domain.ShootingTask;
import com.shootersplatform.backend.training.domain.ShootingTaskId;
import com.shootersplatform.backend.training.domain.Training;
import com.shootersplatform.backend.training.domain.TrainingId;
import com.shootersplatform.backend.training.domain.TrainingRepository;
import com.shootersplatform.backend.training.domain.TrainingSummary;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@NullMarked
@Repository
class JpaTrainingRepository implements TrainingRepository {

    private final SpringDataTrainingRepository trainings;

    JpaTrainingRepository(SpringDataTrainingRepository trainings) {
        this.trainings = trainings;
    }

    @Override
    public List<TrainingSummary> findSummariesByOwner(UserId ownerId) {
        return trainings.findByOwnerUserIdOrderByPerformedOnDescCreatedAtDesc(ownerId.value()).stream()
                .map(entity -> toDomain(entity).toSummary())
                .toList();
    }

    @Override
    public Optional<Training> findByIdAndOwner(TrainingId id, UserId ownerId) {
        return trainings.findByIdAndOwnerUserId(id.value(), ownerId.value()).map(this::toDomain);
    }

    @Override
    public Training save(Training training) {
        return toDomain(trainings.save(toEntity(training)));
    }

    @Override
    public void delete(Training training) {
        trainings.delete(toEntity(training));
    }

    private Training toDomain(TrainingEntity entity) {
        List<ShootingTask> tasks = entity.getTasks().stream()
                .map(this::toDomain)
                .toList();

        return new Training(
                new TrainingId(entity.getId()),
                new UserId(entity.getOwnerUserId()),
                entity.getName(),
                entity.getPlace(),
                entity.getDescription(),
                entity.getPerformedOn(),
                entity.getWeaponType(),
                entity.getScoringType(),
                tasks,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ShootingTask toDomain(ShootingTaskEntity entity) {
        return new ShootingTask(
                new ShootingTaskId(entity.getId()),
                entity.getRunNumber(),
                entity.getWeaponType(),
                new HitScore(entity.getScoringType(), entity.getScore()),
                entity.getDurationTenths()
        );
    }

    private TrainingEntity toEntity(Training training) {
        TrainingEntity entity = new TrainingEntity();
        entity.setId(training.id().value());
        entity.setOwnerUserId(training.ownerId().value());
        entity.setName(training.name());
        entity.setPlace(training.place());
        entity.setDescription(training.description());
        entity.setPerformedOn(training.performedOn());
        entity.setWeaponType(training.weaponType());
        entity.setScoringType(training.scoringType());
        entity.setCreatedAt(training.createdAt());
        entity.setUpdatedAt(training.updatedAt());
        entity.setTasks(training.tasks().stream().map(this::toEntity).toList());
        return entity;
    }

    private ShootingTaskEntity toEntity(ShootingTask task) {
        ShootingTaskEntity entity = new ShootingTaskEntity();
        entity.setId(task.id().value());
        entity.setRunNumber(task.runNumber());
        entity.setWeaponType(task.weaponType());
        entity.setScoringType(task.score().type());
        entity.setDurationTenths(task.durationTenths());
        entity.setScore(task.score().hits());
        return entity;
    }
}
