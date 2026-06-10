package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplate;
import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplateId;
import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplateService;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetTrainingTemplateUseCase {

    private final TrainingTemplateService trainingTemplates;

    GetTrainingTemplateUseCase(TrainingTemplateService trainingTemplates) {
        this.trainingTemplates = trainingTemplates;
    }

    @Transactional(readOnly = true)
    public TrainingTemplate get(UserId ownerId, TrainingTemplateId templateId) {
        return trainingTemplates.get(ownerId, templateId);
    }
}
