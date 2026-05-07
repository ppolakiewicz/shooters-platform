package com.shootersplatform.backend.training.web;

import com.shootersplatform.backend.training.domain.ScoringType;
import com.shootersplatform.backend.training.domain.WeaponType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

record UpsertTrainingRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 120) String place,
        @Size(max = 2048) @Nullable String description,
        @NotNull LocalDate performedOn,
        @NotNull WeaponType weaponType,
        @NotNull ScoringType scoringType
) {
}
