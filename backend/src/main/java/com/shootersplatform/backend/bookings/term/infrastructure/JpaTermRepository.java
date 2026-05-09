package com.shootersplatform.backend.bookings.term.infrastructure;

import com.shootersplatform.backend.bookings.location.domain.Location;
import com.shootersplatform.backend.bookings.term.domain.Term;
import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.term.domain.TermRepository;
import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@NullMarked
@Repository
class JpaTermRepository implements TermRepository {

    private final SpringDataTermRepository repository;

    JpaTermRepository(SpringDataTermRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Term> findPublicTerms() {
        return repository.findAllByOrderByStartsAtAscCreatedAtDesc().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Term> findByOwner(UserId ownerId) {
        return repository.findByOwnerUserIdOrderByStartsAtAsc(ownerId.value()).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Term> findById(TermId id) {
        return repository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Term> findByIdAndOwner(TermId id, UserId ownerId) {
        return repository.findByIdAndOwnerUserId(id.value(), ownerId.value()).map(this::toDomain);
    }

    @Override
    public Optional<Term> findByIdForUpdate(TermId id) {
        return repository.findByIdForUpdate(id.value()).map(this::toDomain);
    }

    @Override
    public Term save(Term term) {
        return toDomain(repository.save(toEntity(term)));
    }

    private Term toDomain(TermEntity entity) {
        return new Term(
                new TermId(entity.getId()),
                new UserId(entity.getOwnerUserId()),
                entity.getName(),
                entity.getDescription(),
                new Location(entity.getPlaceName(), entity.getAddress(), entity.getLatitude(), entity.getLongitude()),
                entity.getCapacity(),
                entity.getCancellationDeadlineDays(),
                entity.getDurationMinutes(),
                entity.getStartsAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private TermEntity toEntity(Term term) {
        TermEntity entity = new TermEntity();
        entity.setId(term.id().value());
        entity.setOwnerUserId(term.ownerId().value());
        entity.setName(term.name());
        entity.setDescription(term.description());
        entity.setPlaceName(term.location().placeName());
        entity.setAddress(term.location().address());
        entity.setLatitude(term.location().latitude());
        entity.setLongitude(term.location().longitude());
        entity.setCapacity(term.capacity());
        entity.setCancellationDeadlineDays(term.cancellationDeadlineDays());
        entity.setDurationMinutes(term.durationMinutes());
        entity.setStartsAt(term.startsAt());
        entity.setCreatedAt(term.createdAt());
        entity.setUpdatedAt(term.updatedAt());
        return entity;
    }
}
