package com.shootersplatform.backend.bookings.waitlist.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "booking_waitlist_entries")
class WaitlistEntryEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "term_id", nullable = false)
    private UUID termId;

    @Nullable
    @Column(name = "participant_user_id")
    private UUID participantUserId;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 40)
    private String phoneNumber;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "cancellation_token", nullable = false, length = 64)
    private String cancellationToken;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    UUID getId() { return id; }
    void setId(UUID id) { this.id = id; }
    UUID getTermId() { return termId; }
    void setTermId(UUID termId) { this.termId = termId; }
    @Nullable UUID getParticipantUserId() { return participantUserId; }
    void setParticipantUserId(@Nullable UUID participantUserId) { this.participantUserId = participantUserId; }
    String getFirstName() { return firstName; }
    void setFirstName(String firstName) { this.firstName = firstName; }
    String getLastName() { return lastName; }
    void setLastName(String lastName) { this.lastName = lastName; }
    String getEmail() { return email; }
    void setEmail(String email) { this.email = email; }
    String getPhoneNumber() { return phoneNumber; }
    void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    int getPosition() { return position; }
    void setPosition(int position) { this.position = position; }
    String getCancellationToken() { return cancellationToken; }
    void setCancellationToken(String cancellationToken) { this.cancellationToken = cancellationToken; }
    Instant getCreatedAt() { return createdAt; }
    void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    Instant getUpdatedAt() { return updatedAt; }
    void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
