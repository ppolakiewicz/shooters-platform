package com.shootersplatform.backend.bookings.trainingtemplate.infrastructure;

import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "booking_training_templates")
class TrainingTemplateEntity {

    @Id
    @Column(name = "id", nullable = false)
    private @Nullable UUID id;

    @Column(name = "owner_user_id", nullable = false)
    private @Nullable UUID ownerUserId;

    @Column(name = "name", nullable = false, length = 120)
    private @Nullable String name;

    @Column(name = "description", nullable = false, length = 2048)
    private @Nullable String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_level", nullable = false, length = 20)
    private @Nullable TrainingLevel trainingLevel;

    @Column(name = "place_name", nullable = false, length = 240)
    private @Nullable String placeName;

    @Column(name = "address", nullable = false, length = 240)
    private @Nullable String address;

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

    @Column(name = "default_start_time", nullable = false)
    private @Nullable LocalTime defaultStartTime;

    @Column(name = "created_at", nullable = false)
    private @Nullable Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private @Nullable Instant updatedAt;

    UUID getId() {
        return Objects.requireNonNull(id);
    }

    void setId(UUID id) {
        this.id = id;
    }

    UUID getOwnerUserId() {
        return Objects.requireNonNull(ownerUserId);
    }

    void setOwnerUserId(UUID ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    String getName() {
        return Objects.requireNonNull(name);
    }

    void setName(String name) {
        this.name = name;
    }

    String getDescription() {
        return Objects.requireNonNull(description);
    }

    void setDescription(String description) {
        this.description = description;
    }

    TrainingLevel getTrainingLevel() {
        return Objects.requireNonNull(trainingLevel);
    }

    void setTrainingLevel(TrainingLevel trainingLevel) {
        this.trainingLevel = trainingLevel;
    }

    String getPlaceName() {
        return Objects.requireNonNull(placeName);
    }

    void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    String getAddress() {
        return Objects.requireNonNull(address);
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

    LocalTime getDefaultStartTime() {
        return Objects.requireNonNull(defaultStartTime);
    }

    void setDefaultStartTime(LocalTime defaultStartTime) {
        this.defaultStartTime = defaultStartTime;
    }

    Instant getCreatedAt() {
        return Objects.requireNonNull(createdAt);
    }

    void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    Instant getUpdatedAt() {
        return Objects.requireNonNull(updatedAt);
    }

    void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
