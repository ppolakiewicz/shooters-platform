package com.shootersplatform.backend.bookings.trainingtemplate.domain;

import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.NullMarked;

import java.util.*;

@NullMarked
public class InMemoryTrainingTemplateRepository implements TrainingTemplateRepository {

    private final Map<TrainingTemplateId, TrainingTemplate> templates = new HashMap<>();

    @Override
    public List<TrainingTemplate> findByOwner(UserId ownerId) {
        return templates.values().stream()
                .filter(template -> template.ownerId().equals(ownerId))
                .sorted(Comparator.comparing(TrainingTemplate::updatedAt).reversed())
                .toList();
    }

    @Override
    public Optional<TrainingTemplate> findByIdAndOwner(TrainingTemplateId id, UserId ownerId) {
        return Optional.ofNullable(templates.get(id))
                .filter(template -> template.ownerId().equals(ownerId));
    }

    @Override
    public TrainingTemplate save(TrainingTemplate trainingTemplate) {
        templates.put(trainingTemplate.id(), trainingTemplate);
        return trainingTemplate;
    }

    @Override
    public void delete(TrainingTemplate trainingTemplate) {
        templates.remove(trainingTemplate.id());
    }
}
