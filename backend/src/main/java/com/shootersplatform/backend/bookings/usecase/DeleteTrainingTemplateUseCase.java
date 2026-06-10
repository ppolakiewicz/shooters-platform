package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplateId;
import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplateService;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteTrainingTemplateUseCase {

    private final TrainingTemplateService trainingTemplates;

    DeleteTrainingTemplateUseCase(TrainingTemplateService trainingTemplates) {
        this.trainingTemplates = trainingTemplates;
    }

    @Transactional
    public void delete(UserId ownerId, TrainingTemplateId templateId) {
        trainingTemplates.delete(ownerId, templateId);
    }
}
