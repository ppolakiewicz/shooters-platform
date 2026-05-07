package com.shootersplatform.backend.training.domain;

import java.io.Serializable;
import java.util.UUID;

public record TrainingId(UUID value) implements Serializable {

    public static TrainingId newId() {
        return new TrainingId(UUID.randomUUID());
    }
}
