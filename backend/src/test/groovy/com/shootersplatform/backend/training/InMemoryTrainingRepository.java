package com.shootersplatform.backend.training;

import com.shootersplatform.backend.identity.domain.UserId;
import com.shootersplatform.backend.training.domain.Training;
import com.shootersplatform.backend.training.domain.TrainingId;
import com.shootersplatform.backend.training.domain.TrainingRepository;
import com.shootersplatform.backend.training.domain.TrainingSummary;
import org.jspecify.annotations.NullMarked;

import java.util.*;

@NullMarked
public class InMemoryTrainingRepository implements TrainingRepository {

    private final Map<TrainingId, Training> trainings = new HashMap<>();

    @Override
    public List<TrainingSummary> findSummariesByOwner(UserId ownerId) {
        return trainings.values().stream()
                .filter(training -> training.ownerId().equals(ownerId))
                .map(Training::toSummary)
                .sorted(Comparator.comparing(TrainingSummary::performedOn).reversed()
                        .thenComparing(TrainingSummary::createdAt, Comparator.reverseOrder()))
                .toList();
    }

    @Override
    public Optional<Training> findByIdAndOwner(TrainingId id, UserId ownerId) {
        return Optional.ofNullable(trainings.get(id))
                .filter(training -> training.ownerId().equals(ownerId));
    }

    @Override
    public Training save(Training training) {
        trainings.put(training.id(), training);
        return training;
    }

    @Override
    public void delete(Training training) {
        trainings.remove(training.id());
    }

    public int count() {
        return trainings.size();
    }
}
