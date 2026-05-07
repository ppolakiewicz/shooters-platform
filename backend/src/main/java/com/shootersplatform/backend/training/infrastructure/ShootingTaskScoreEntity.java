package com.shootersplatform.backend.training.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "shooting_task_scores")
class ShootingTaskScoreEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shooting_task_id", nullable = false)
    private ShootingTaskEntity task;

    @Column(name = "score_key", nullable = false, length = 32)
    private String scoreKey;

    @Column(name = "hit_count", nullable = false)
    private int hitCount;

    UUID getId() {
        return id;
    }

    void setId(UUID id) {
        this.id = id;
    }

    ShootingTaskEntity getTask() {
        return task;
    }

    void setTask(ShootingTaskEntity task) {
        this.task = task;
    }

    String getScoreKey() {
        return scoreKey;
    }

    void setScoreKey(String scoreKey) {
        this.scoreKey = scoreKey;
    }

    int getHitCount() {
        return hitCount;
    }

    void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }
}
