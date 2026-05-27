package com.shootersplatform.backend.bookings.trainingenrollment.domain;

import com.shootersplatform.backend.bookings.location.domain.Location;
import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
@Transactional
public class TrainingEnrollmentService {

    private final TrainingEnrollmentRepository trainingEnrollments;
    private final Clock clock;

    public TrainingEnrollmentService(TrainingEnrollmentRepository trainingEnrollments, Clock clock) {
        this.trainingEnrollments = trainingEnrollments;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<TrainingEnrollment> list(UserId ownerId) {
        return trainingEnrollments.findByOwner(ownerId);
    }

    public TrainingEnrollment create(
            UserId ownerId,
            String name,
            String description,
            TrainingLevel trainingLevel,
            Location location,
            int capacity,
            int cancellationDeadlineDays,
            int durationMinutes
    ) {
        return trainingEnrollments.save(TrainingEnrollment.create(ownerId, name, description, trainingLevel, location, capacity, cancellationDeadlineDays, durationMinutes, clock.instant()));
    }

    public TrainingEnrollment update(
            UserId ownerId,
            TrainingEnrollmentId enrollmentId,
            String name,
            String description,
            TrainingLevel trainingLevel,
            Location location,
            int capacity,
            int cancellationDeadlineDays,
            int durationMinutes
    ) {
        TrainingEnrollment enrollment = trainingEnrollments.findByIdAndOwner(enrollmentId, ownerId).orElseThrow(TrainingEnrollmentNotFoundException::new);
        return trainingEnrollments.save(enrollment.update(name, description, trainingLevel, location, capacity, cancellationDeadlineDays, durationMinutes, clock.instant()));
    }
}
