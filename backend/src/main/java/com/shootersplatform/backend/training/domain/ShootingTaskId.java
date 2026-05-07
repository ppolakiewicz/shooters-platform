package com.shootersplatform.backend.training.domain;

import java.io.Serializable;
import java.util.UUID;

public record ShootingTaskId(UUID value) implements Serializable {

    public static ShootingTaskId newId() {
        return new ShootingTaskId(UUID.randomUUID());
    }
}
