package com.shootersplatform.backend.bookings.trainingtemplate.infrastructure;

import com.shootersplatform.backend.bookings.location.domain.Location;
import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplate;
import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplateId;
import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplateRepository;
import com.shootersplatform.backend.identity.domain.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class JpaTrainingTemplateRepository implements TrainingTemplateRepository {

    private final SpringDataTrainingTemplateRepository repository;

    JpaTrainingTemplateRepository(SpringDataTrainingTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TrainingTemplate> findByOwner(UserId ownerId) {
        return repository.findByOwnerUserIdOrderByUpdatedAtDesc(ownerId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<TrainingTemplate> findByIdAndOwner(TrainingTemplateId id, UserId ownerId) {
        return repository.findByIdAndOwnerUserId(id.value(), ownerId.value()).map(this::toDomain);
    }

    @Override
    public TrainingTemplate save(TrainingTemplate trainingTemplate) {
        return toDomain(repository.save(toEntity(trainingTemplate)));
    }

    @Override
    public void delete(TrainingTemplate trainingTemplate) {
        repository.deleteById(trainingTemplate.id().value());
    }

    private TrainingTemplate toDomain(TrainingTemplateEntity entity) {
        return new TrainingTemplate(
                new TrainingTemplateId(entity.getId()),
                new UserId(entity.getOwnerUserId()),
                entity.getName(),
                entity.getDescription(),
                entity.getTrainingLevel(),
                new Location(entity.getPlaceName(), entity.getAddress(), entity.getLatitude(), entity.getLongitude()),
                entity.getCapacity(),
                entity.getCancellationDeadlineDays(),
                entity.getDurationMinutes(),
                entity.getDefaultStartTime(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private TrainingTemplateEntity toEntity(TrainingTemplate template) {
        TrainingTemplateEntity entity = new TrainingTemplateEntity();
        entity.setId(template.id().value());
        entity.setOwnerUserId(template.ownerId().value());
        entity.setName(template.name());
        entity.setDescription(template.description());
        entity.setTrainingLevel(template.trainingLevel());
        entity.setPlaceName(template.location().placeName());
        entity.setAddress(template.location().address());
        entity.setLatitude(template.location().latitude());
        entity.setLongitude(template.location().longitude());
        entity.setCapacity(template.capacity());
        entity.setCancellationDeadlineDays(template.cancellationDeadlineDays());
        entity.setDurationMinutes(template.durationMinutes());
        entity.setDefaultStartTime(template.defaultStartTime());
        entity.setCreatedAt(template.createdAt());
        entity.setUpdatedAt(template.updatedAt());
        return entity;
    }
}
