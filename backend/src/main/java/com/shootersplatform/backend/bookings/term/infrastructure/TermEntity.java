package com.shootersplatform.backend.bookings.term.infrastructure;

import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "booking_terms")
class TermEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", nullable = false, length = 2048)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_level", nullable = false, length = 20)
    private TrainingLevel trainingLevel;

    @Column(name = "place_name", nullable = false, length = 240)
    private String placeName;

    @Column(name = "address", nullable = false, length = 240)
    private String address;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "cancellation_deadline_days", nullable = false)
    private int cancellationDeadlineDays;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    UUID getId() {
        return id;
    }

    void setId(UUID id) {
        this.id = id;
    }

    UUID getOwnerUserId() {
        return ownerUserId;
    }

    void setOwnerUserId(UUID ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    TrainingLevel getTrainingLevel() {
        return trainingLevel;
    }

    void setTrainingLevel(TrainingLevel trainingLevel) {
        this.trainingLevel = trainingLevel;
    }

    String getPlaceName() {
        return placeName;
    }

    void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    String getAddress() {
        return address;
    }

    void setAddress(String address) {
        this.address = address;
    }

    double getLatitude() {
        return latitude;
    }

    void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    double getLongitude() {
        return longitude;
    }

    void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    int getCapacity() {
        return capacity;
    }

    void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    int getCancellationDeadlineDays() {
        return cancellationDeadlineDays;
    }

    void setCancellationDeadlineDays(int cancellationDeadlineDays) {
        this.cancellationDeadlineDays = cancellationDeadlineDays;
    }

    int getDurationMinutes() {
        return durationMinutes;
    }

    void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    LocalDateTime getStartsAt() {
        return startsAt;
    }

    void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }

    void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
