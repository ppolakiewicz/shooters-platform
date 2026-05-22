package com.shootersplatform.backend.training.infrastructure;

import com.shootersplatform.backend.training.domain.ScoringType;
import com.shootersplatform.backend.training.domain.WeaponType;
import jakarta.persistence.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Entity
@Table(name = "shooting_tasks")
class ShootingTaskEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_id", nullable = false)
    private TrainingEntity training;

    @Column(name = "run_number", nullable = false)
    private int runNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "weapon_type", nullable = false, length = 32)
    private WeaponType weaponType;

    @Enumerated(EnumType.STRING)
    @Column(name = "scoring_type", nullable = false, length = 32)
    private ScoringType scoringType;

    @Column(name = "duration_tenths", nullable = false)
    private int durationTenths;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("scoreKey ASC")
    private List<ShootingTaskScoreEntity> scoreEntries = new ArrayList<>();

    UUID getId() {
        return id;
    }

    void setId(UUID id) {
        this.id = id;
    }

    TrainingEntity getTraining() {
        return training;
    }

    void setTraining(TrainingEntity training) {
        this.training = training;
    }

    int getRunNumber() {
        return runNumber;
    }

    void setRunNumber(int runNumber) {
        this.runNumber = runNumber;
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

    int getDurationTenths() {
        return durationTenths;
    }

    void setDurationTenths(int durationTenths) {
        this.durationTenths = durationTenths;
    }

    Map<String, Integer> getScore() {
        Map<String, Integer> score = new HashMap<>();
        scoreEntries.forEach(entry -> score.put(entry.getScoreKey(), entry.getHitCount()));
        return score;
    }

    void setScore(Map<String, Integer> score) {
        this.scoreEntries.clear();
        score.forEach((key, value) -> {
            ShootingTaskScoreEntity entry = new ShootingTaskScoreEntity();
            entry.setId(scoreEntryId(key));
            entry.setScoreKey(key);
            entry.setHitCount(value);
            addScoreEntry(entry);
        });
    }

    void addScoreEntry(ShootingTaskScoreEntity entry) {
        entry.setTask(this);
        this.scoreEntries.add(entry);
    }

    private UUID scoreEntryId(String scoreKey) {
        return UUID.nameUUIDFromBytes("%s:%s".formatted(id, scoreKey).getBytes(StandardCharsets.UTF_8));
    }
}
