package com.shootersplatform.backend.bookings;

import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollment;
import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollmentId;
import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollmentRepository;
import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.NullMarked;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@NullMarked
public class InMemoryTrainingEnrollmentRepository implements TrainingEnrollmentRepository {

    private final Map<TrainingEnrollmentId, TrainingEnrollment> enrollments = new HashMap<>();

    @Override
    public List<TrainingEnrollment> findByOwner(UserId ownerId) {
        return enrollments.values().stream()
                .filter(enrollment -> enrollment.ownerId().equals(ownerId))
                .sorted(Comparator.comparing(TrainingEnrollment::createdAt).reversed())
                .toList();
    }

    @Override
    public Optional<TrainingEnrollment> findByIdAndOwner(TrainingEnrollmentId id, UserId ownerId) {
        return Optional.ofNullable(enrollments.get(id)).filter(enrollment -> enrollment.ownerId().equals(ownerId));
    }

    @Override
    public TrainingEnrollment save(TrainingEnrollment trainingEnrollment) {
        enrollments.put(trainingEnrollment.id(), trainingEnrollment);
        return trainingEnrollment;
    }
}
