package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplate;
import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplateService;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListTrainingTemplatesUseCase {

    private final TrainingTemplateService trainingTemplates;

    ListTrainingTemplatesUseCase(TrainingTemplateService trainingTemplates) {
        this.trainingTemplates = trainingTemplates;
    }

    @Transactional(readOnly = true)
    public List<TrainingTemplate> list(UserId ownerId) {
        return trainingTemplates.list(ownerId);
    }
}
