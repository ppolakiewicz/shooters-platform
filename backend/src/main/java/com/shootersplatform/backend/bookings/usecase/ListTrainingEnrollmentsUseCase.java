package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollment;
import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollmentService;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListTrainingEnrollmentsUseCase {

    private final TrainingEnrollmentService trainingEnrollments;

    public ListTrainingEnrollmentsUseCase(TrainingEnrollmentService trainingEnrollments) {
        this.trainingEnrollments = trainingEnrollments;
    }

    @Transactional(readOnly = true)
    public List<TrainingEnrollment> list(UserId ownerId) {
        return trainingEnrollments.list(ownerId);
    }
}
