package com.shootersplatform.backend.bookings.waitlist.infrastructure;

import com.shootersplatform.backend.bookings.term.domain.TermId;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistEntry;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistEntryId;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistRepository;
import com.shootersplatform.backend.identity.domain.EmailAddress;
import com.shootersplatform.backend.identity.domain.UserId;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@NullMarked
@Repository
class JpaWaitlistRepository implements WaitlistRepository {

    private final SpringDataWaitlistRepository repository;

    JpaWaitlistRepository(SpringDataWaitlistRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<WaitlistEntry> findByTerm(TermId termId) {
        return repository.findByTermIdOrderByPositionAscCreatedAtAsc(termId.value()).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<WaitlistEntry> findByIdAndTerm(WaitlistEntryId entryId, TermId termId) {
        return repository.findByIdAndTermId(entryId.value(), termId.value()).map(this::toDomain);
    }

    @Override
    public Optional<WaitlistEntry> findByCancellationToken(String token) {
        return repository.findByCancellationToken(token).map(this::toDomain);
    }

    @Override
    public Optional<WaitlistEntry> findFirstByTerm(TermId termId) {
        return repository.findFirstByTermIdOrderByPositionAscCreatedAtAsc(termId.value()).map(this::toDomain);
    }

    @Override
    public boolean existsByTermAndEmail(TermId termId, EmailAddress email) {
        return repository.existsByTermIdAndEmail(termId.value(), email.value());
    }

    @Override
    public int nextPosition(TermId termId) {
        Integer maxPosition = repository.findMaxPosition(termId.value());
        return (maxPosition == null ? 0 : maxPosition) + 1;
    }

    @Override
    public WaitlistEntry save(WaitlistEntry entry) {
        return toDomain(repository.save(toEntity(entry)));
    }

    @Override
    public void delete(WaitlistEntry entry) {
        repository.deleteById(entry.id().value());
    }

    private WaitlistEntry toDomain(WaitlistEntryEntity entity) {
        return new WaitlistEntry(
                new WaitlistEntryId(entity.getId()),
                new TermId(entity.getTermId()),
                entity.getParticipantUserId() == null ? null : new UserId(entity.getParticipantUserId()),
                entity.getFirstName(),
                entity.getLastName(),
                new EmailAddress(entity.getEmail()),
                entity.getPhoneNumber(),
                entity.getPosition(),
                entity.getCancellationToken(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private WaitlistEntryEntity toEntity(WaitlistEntry entry) {
        WaitlistEntryEntity entity = new WaitlistEntryEntity();
        entity.setId(entry.id().value());
        entity.setTermId(entry.termId().value());
        entity.setParticipantUserId(entry.participantUserId() == null ? null : entry.participantUserId().value());
        entity.setFirstName(entry.firstName());
        entity.setLastName(entry.lastName());
        entity.setEmail(entry.email().value());
        entity.setPhoneNumber(entry.phoneNumber());
        entity.setPosition(entry.position());
        entity.setCancellationToken(entry.cancellationToken());
        entity.setCreatedAt(entry.createdAt());
        entity.setUpdatedAt(entry.updatedAt());
        return entity;
    }
}
