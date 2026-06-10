package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.location.domain.Location;
import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel;
import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplate;
import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplateId;
import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplateService;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
public class UpdateTrainingTemplateUseCase {

    private final TrainingTemplateService trainingTemplates;

    UpdateTrainingTemplateUseCase(TrainingTemplateService trainingTemplates) {
        this.trainingTemplates = trainingTemplates;
    }

    @Transactional
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
        return trainingTemplates.update(
                ownerId,
                templateId,
                name,
                description,
                trainingLevel,
                location,
                capacity,
                cancellationDeadlineDays,
                durationMinutes,
                defaultStartTime
        );
    }
}
