package com.shootersplatform.backend.bookings.trainingtemplate.domain;

import com.shootersplatform.backend.bookings.location.domain.Location;
import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalTime;
import java.util.List;

@Service
public class TrainingTemplateService {

    private final TrainingTemplateRepository trainingTemplates;
    private final Clock clock;

    TrainingTemplateService(TrainingTemplateRepository trainingTemplates, Clock clock) {
        this.trainingTemplates = trainingTemplates;
        this.clock = clock;
    }

    public List<TrainingTemplate> list(UserId ownerId) {
        return trainingTemplates.findByOwner(ownerId);
    }

    public TrainingTemplate get(UserId ownerId, TrainingTemplateId templateId) {
        return requireOwned(ownerId, templateId);
    }

    public TrainingTemplate create(
            UserId ownerId,
            String name,
            String description,
            TrainingLevel trainingLevel,
            Location location,
            int capacity,
            int cancellationDeadlineDays,
            int durationMinutes,
            LocalTime defaultStartTime
    ) {
        return trainingTemplates.save(TrainingTemplate.create(
                ownerId,
                name,
                description,
                trainingLevel,
                location,
                capacity,
                cancellationDeadlineDays,
                durationMinutes,
                defaultStartTime,
                clock.instant()
        ));
    }

    public TrainingTemplate update(
            UserId ownerId,
            TrainingTemplateId templateId,
            String name,
            String description,
            TrainingLevel trainingLevel,
            Location location,
            int capacity,
            int cancellationDeadlineDays,
            int durationMinutes,
            LocalTime defaultStartTime
    ) {
        TrainingTemplate template = requireOwned(ownerId, templateId);
        return trainingTemplates.save(template.update(
                name,
                description,
                trainingLevel,
                location,
                capacity,
                cancellationDeadlineDays,
                durationMinutes,
                defaultStartTime,
                clock.instant()
        ));
    }

    public void delete(UserId ownerId, TrainingTemplateId templateId) {
        trainingTemplates.delete(requireOwned(ownerId, templateId));
    }

    private TrainingTemplate requireOwned(UserId ownerId, TrainingTemplateId templateId) {
        return trainingTemplates.findByIdAndOwner(templateId, ownerId)
                .orElseThrow(TrainingTemplateNotFoundException::new);
    }
}
