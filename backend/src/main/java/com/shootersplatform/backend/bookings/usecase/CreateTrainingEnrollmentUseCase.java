package com.shootersplatform.backend.bookings.usecase;

import com.shootersplatform.backend.bookings.location.domain.Location;
import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollment;
import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollmentService;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateTrainingEnrollmentUseCase {

    private final TrainingEnrollmentService trainingEnrollments;

    CreateTrainingEnrollmentUseCase(TrainingEnrollmentService trainingEnrollments) {
        this.trainingEnrollments = trainingEnrollments;
    }

    @Transactional
    public TrainingEnrollment create(
            UserId ownerId,
            String name,
            String description,
            Location location,
            int capacity,
            int cancellationDeadlineDays,
            int durationMinutes
    ) {
        return trainingEnrollments.create(ownerId, name, description, location, capacity, cancellationDeadlineDays, durationMinutes);
    }
}
