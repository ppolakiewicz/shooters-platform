package com.shootersplatform.backend.bookings.trainingtemplate.domain;

import com.shootersplatform.backend.identity.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface TrainingTemplateRepository {

    List<TrainingTemplate> findByOwner(UserId ownerId);

    Optional<TrainingTemplate> findByIdAndOwner(TrainingTemplateId id, UserId ownerId);

    TrainingTemplate save(TrainingTemplate trainingTemplate);

    void delete(TrainingTemplate trainingTemplate);
}
