package com.shootersplatform.backend.bookings.web;

import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplateId;
import com.shootersplatform.backend.bookings.usecase.*;
import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings/training-templates")
@PreAuthorize("hasRole('ORGANIZER')")
class BookingTrainingTemplatesController {

    private final ListTrainingTemplatesUseCase listTrainingTemplates;
    private final GetTrainingTemplateUseCase getTrainingTemplate;
    private final CreateTrainingTemplateUseCase createTrainingTemplate;
    private final UpdateTrainingTemplateUseCase updateTrainingTemplate;
    private final DeleteTrainingTemplateUseCase deleteTrainingTemplate;

    BookingTrainingTemplatesController(
            ListTrainingTemplatesUseCase listTrainingTemplates,
            GetTrainingTemplateUseCase getTrainingTemplate,
            CreateTrainingTemplateUseCase createTrainingTemplate,
            UpdateTrainingTemplateUseCase updateTrainingTemplate,
            DeleteTrainingTemplateUseCase deleteTrainingTemplate
    ) {
        this.listTrainingTemplates = listTrainingTemplates;
        this.getTrainingTemplate = getTrainingTemplate;
        this.createTrainingTemplate = createTrainingTemplate;
        this.updateTrainingTemplate = updateTrainingTemplate;
        this.deleteTrainingTemplate = deleteTrainingTemplate;
    }

    @GetMapping
    List<TrainingTemplateResponse> list(Authentication authentication) {
        return listTrainingTemplates.list(currentUser(authentication).id()).stream()
                .map(TrainingTemplateResponse::from)
                .toList();
    }

    @GetMapping("/{templateId}")
    TrainingTemplateResponse get(@PathVariable UUID templateId, Authentication authentication) {
        return TrainingTemplateResponse.from(getTrainingTemplate.get(
                currentUser(authentication).id(),
                new TrainingTemplateId(templateId)
        ));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TrainingTemplateResponse create(
            @Valid @RequestBody TrainingTemplateRequest request,
            Authentication authentication
    ) {
        return TrainingTemplateResponse.from(createTrainingTemplate.create(
                currentUser(authentication).id(),
                request.name(),
                request.description(),
                request.trainingLevel(),
                request.location().toDomain(),
                request.capacity(),
                request.cancellationDeadlineDays(),
                request.durationMinutes(),
                request.defaultStartTime()
        ));
    }

    @PutMapping("/{templateId}")
    TrainingTemplateResponse update(
            @PathVariable UUID templateId,
            @Valid @RequestBody TrainingTemplateRequest request,
            Authentication authentication
    ) {
        return TrainingTemplateResponse.from(updateTrainingTemplate.update(
                currentUser(authentication).id(),
                new TrainingTemplateId(templateId),
                request.name(),
                request.description(),
                request.trainingLevel(),
                request.location().toDomain(),
                request.capacity(),
                request.cancellationDeadlineDays(),
                request.durationMinutes(),
                request.defaultStartTime()
        ));
    }

    @DeleteMapping("/{templateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID templateId, Authentication authentication) {
        deleteTrainingTemplate.delete(currentUser(authentication).id(), new TrainingTemplateId(templateId));
    }

    private static AuthenticatedUser currentUser(@Nullable Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return user;
    }
}
