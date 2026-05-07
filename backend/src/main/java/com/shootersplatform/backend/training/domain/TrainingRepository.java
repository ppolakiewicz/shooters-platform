package com.shootersplatform.backend.training.domain;

import com.shootersplatform.backend.identity.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface TrainingRepository {

    List<TrainingSummary> findSummariesByOwner(UserId ownerId);

    Optional<Training> findByIdAndOwner(TrainingId id, UserId ownerId);

    Training save(Training training);

    void delete(Training training);
}
