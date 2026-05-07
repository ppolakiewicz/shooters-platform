package com.shootersplatform.backend.training.infrastructure;

import com.shootersplatform.backend.training.domain.ScoringType;
import com.shootersplatform.backend.training.domain.WeaponType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trainings")
class TrainingEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "place", nullable = false, length = 120)
    private String place;

    @Column(name = "description", nullable = false, length = 2048)
    private String description;

    @Column(name = "performed_on", nullable = false)
    private LocalDate performedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "weapon_type", nullable = false, length = 32)
    private WeaponType weaponType;

    @Enumerated(EnumType.STRING)
    @Column(name = "scoring_type", nullable = false, length = 32)
    private ScoringType scoringType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "training", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("runNumber ASC")
    private List<ShootingTaskEntity> tasks = new ArrayList<>();

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

    String getPlace() {
        return place;
    }

    void setPlace(String place) {
        this.place = place;
    }

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    LocalDate getPerformedOn() {
        return performedOn;
    }

    void setPerformedOn(LocalDate performedOn) {
        this.performedOn = performedOn;
    }

    WeaponType getWeaponType() {
        return weaponType;
    }

    void setWeaponType(WeaponType weaponType) {
        this.weaponType = weaponType;
    }

    ScoringType getScoringType() {
        return scoringType;
    }

    void setScoringType(ScoringType scoringType) {
        this.scoringType = scoringType;
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

    List<ShootingTaskEntity> getTasks() {
        return tasks;
    }

    void setTasks(List<ShootingTaskEntity> tasks) {
        this.tasks.clear();
        tasks.forEach(this::addTask);
    }

    void addTask(ShootingTaskEntity task) {
        task.setTraining(this);
        this.tasks.add(task);
    }
}
