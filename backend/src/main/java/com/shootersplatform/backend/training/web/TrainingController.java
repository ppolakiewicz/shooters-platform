package com.shootersplatform.backend.training.web;

import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import com.shootersplatform.backend.training.domain.ShootingTaskId;
import com.shootersplatform.backend.training.domain.TrainingId;
import com.shootersplatform.backend.training.domain.TrainingService;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trainings")
@PreAuthorize("hasRole('USER')")
class TrainingController {

    private final TrainingService trainings;

    TrainingController(TrainingService trainings) {
        this.trainings = trainings;
    }

    @GetMapping
    List<TrainingSummaryResponse> list(Authentication authentication) {
        return trainings.list(currentUser(authentication).id()).stream()
                .map(TrainingSummaryResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TrainingResponse create(@Valid @RequestBody UpsertTrainingRequest request, Authentication authentication) {
        return TrainingResponse.from(trainings.create(
                currentUser(authentication).id(),
                request.name(),
                request.place(),
                optionalText(request.description()),
                request.performedOn(),
                request.weaponType(),
                request.scoringType()
        ));
    }

    @GetMapping("/{trainingId}")
    TrainingResponse get(@PathVariable UUID trainingId, Authentication authentication) {
        return TrainingResponse.from(trainings.get(currentUser(authentication).id(), new TrainingId(trainingId)));
    }

    @PutMapping("/{trainingId}")
    TrainingResponse update(
            @PathVariable UUID trainingId,
            @Valid @RequestBody UpsertTrainingRequest request,
            Authentication authentication
    ) {
        return TrainingResponse.from(trainings.update(
                currentUser(authentication).id(),
                new TrainingId(trainingId),
                request.name(),
                request.place(),
                optionalText(request.description()),
                request.performedOn(),
                request.weaponType(),
                request.scoringType()
        ));
    }

    @DeleteMapping("/{trainingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID trainingId, Authentication authentication) {
        trainings.delete(currentUser(authentication).id(), new TrainingId(trainingId));
    }

    @PostMapping("/{trainingId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    TrainingResponse addTask(
            @PathVariable UUID trainingId,
            @Valid @RequestBody UpsertTaskRequest request,
            Authentication authentication
    ) {
        return TrainingResponse.from(trainings.addTask(
                currentUser(authentication).id(),
                new TrainingId(trainingId),
                request.weaponType(),
                request.toDomainScore(),
                request.durationTenths()
        ));
    }

    @PutMapping("/{trainingId}/tasks/{taskId}")
    TrainingResponse updateTask(
            @PathVariable UUID trainingId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpsertTaskRequest request,
            Authentication authentication
    ) {
        return TrainingResponse.from(trainings.updateTask(
                currentUser(authentication).id(),
                new TrainingId(trainingId),
                new ShootingTaskId(taskId),
                request.weaponType(),
                request.toDomainScore(),
                request.durationTenths()
        ));
    }

    @DeleteMapping("/{trainingId}/tasks/{taskId}")
    TrainingResponse deleteTask(
            @PathVariable UUID trainingId,
            @PathVariable UUID taskId,
            Authentication authentication
    ) {
        return TrainingResponse.from(trainings.deleteTask(
                currentUser(authentication).id(),
                new TrainingId(trainingId),
                new ShootingTaskId(taskId)
        ));
    }

    private static AuthenticatedUser currentUser(@Nullable Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return user;
    }

    private static String optionalText(@Nullable String value) {
        return value == null ? "" : value;
    }
}
