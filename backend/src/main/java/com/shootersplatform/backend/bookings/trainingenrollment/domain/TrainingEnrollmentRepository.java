package com.shootersplatform.backend.bookings.trainingenrollment.domain;

import com.shootersplatform.backend.identity.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface TrainingEnrollmentRepository {

    List<TrainingEnrollment> findByOwner(UserId ownerId);

    Optional<TrainingEnrollment> findByIdAndOwner(TrainingEnrollmentId id, UserId ownerId);

    TrainingEnrollment save(TrainingEnrollment trainingEnrollment);
}
