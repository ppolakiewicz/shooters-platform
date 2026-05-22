package com.shootersplatform.backend.bookings.web;

import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollmentId;
import com.shootersplatform.backend.bookings.usecase.CreateTrainingEnrollmentUseCase;
import com.shootersplatform.backend.bookings.usecase.ListTrainingEnrollmentsUseCase;
import com.shootersplatform.backend.bookings.usecase.UpdateTrainingEnrollmentUseCase;
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
@RequestMapping("/api/bookings/training-enrollments")
@PreAuthorize("hasRole('USER')")
class BookingTrainingEnrollmentsController {

    private final ListTrainingEnrollmentsUseCase listTrainingEnrollments;
    private final CreateTrainingEnrollmentUseCase createTrainingEnrollment;
    private final UpdateTrainingEnrollmentUseCase updateTrainingEnrollment;

    BookingTrainingEnrollmentsController(
        ListTrainingEnrollmentsUseCase listTrainingEnrollments,
        CreateTrainingEnrollmentUseCase createTrainingEnrollment,
        UpdateTrainingEnrollmentUseCase updateTrainingEnrollment
    ) {
        this.listTrainingEnrollments = listTrainingEnrollments;
        this.createTrainingEnrollment = createTrainingEnrollment;
        this.updateTrainingEnrollment = updateTrainingEnrollment;
    }

    @GetMapping
    List<TrainingEnrollmentResponse> list(Authentication authentication) {
        return listTrainingEnrollments.list(currentUser(authentication).id()).stream()
                .map(TrainingEnrollmentResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TrainingEnrollmentResponse create(@Valid @RequestBody TrainingEnrollmentRequest request, Authentication authentication) {
        return TrainingEnrollmentResponse.from(createTrainingEnrollment.create(
                currentUser(authentication).id(),
                request.name(),
                request.description(),
                request.location().toDomain(),
                request.capacity(),
                request.cancellationDeadlineDays(),
                request.durationMinutes()
        ));
    }

    @PutMapping("/{enrollmentId}")
    TrainingEnrollmentResponse update(
            @PathVariable UUID enrollmentId,
            @Valid @RequestBody TrainingEnrollmentRequest request,
            Authentication authentication
    ) {
        return TrainingEnrollmentResponse.from(updateTrainingEnrollment.update(
                currentUser(authentication).id(),
                new TrainingEnrollmentId(enrollmentId),
                request.name(),
                request.description(),
                request.location().toDomain(),
                request.capacity(),
                request.cancellationDeadlineDays(),
                request.durationMinutes()
        ));
    }

    private static AuthenticatedUser currentUser(@Nullable Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return user;
    }
}
