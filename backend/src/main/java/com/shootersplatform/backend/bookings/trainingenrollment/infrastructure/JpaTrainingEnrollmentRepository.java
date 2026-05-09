package com.shootersplatform.backend.bookings.trainingenrollment.infrastructure;

import com.shootersplatform.backend.bookings.location.domain.Location;
import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollment;
import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollmentId;
import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollmentRepository;
import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@NullMarked
@Repository
class JpaTrainingEnrollmentRepository implements TrainingEnrollmentRepository {

    private final SpringDataTrainingEnrollmentRepository repository;

    JpaTrainingEnrollmentRepository(SpringDataTrainingEnrollmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TrainingEnrollment> findByOwner(UserId ownerId) {
        return repository.findByOwnerUserIdOrderByCreatedAtDesc(ownerId.value()).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<TrainingEnrollment> findByIdAndOwner(TrainingEnrollmentId id, UserId ownerId) {
        return repository.findByIdAndOwnerUserId(id.value(), ownerId.value()).map(this::toDomain);
    }

    @Override
    public TrainingEnrollment save(TrainingEnrollment trainingEnrollment) {
        return toDomain(repository.save(toEntity(trainingEnrollment)));
    }

    private TrainingEnrollment toDomain(TrainingEnrollmentEntity entity) {
        return new TrainingEnrollment(
                new TrainingEnrollmentId(entity.getId()),
                new UserId(entity.getOwnerUserId()),
                entity.getName(),
                entity.getDescription(),
                new Location(entity.getPlaceName(), entity.getAddress(), entity.getLatitude(), entity.getLongitude()),
                entity.getCapacity(),
                entity.getCancellationDeadlineDays(),
                entity.getDurationMinutes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private TrainingEnrollmentEntity toEntity(TrainingEnrollment enrollment) {
        TrainingEnrollmentEntity entity = new TrainingEnrollmentEntity();
        entity.setId(enrollment.id().value());
        entity.setOwnerUserId(enrollment.ownerId().value());
        entity.setName(enrollment.name());
        entity.setDescription(enrollment.description());
        entity.setPlaceName(enrollment.location().placeName());
        entity.setAddress(enrollment.location().address());
        entity.setLatitude(enrollment.location().latitude());
        entity.setLongitude(enrollment.location().longitude());
        entity.setCapacity(enrollment.capacity());
        entity.setCancellationDeadlineDays(enrollment.cancellationDeadlineDays());
        entity.setDurationMinutes(enrollment.durationMinutes());
        entity.setCreatedAt(enrollment.createdAt());
        entity.setUpdatedAt(enrollment.updatedAt());
        return entity;
    }
}
