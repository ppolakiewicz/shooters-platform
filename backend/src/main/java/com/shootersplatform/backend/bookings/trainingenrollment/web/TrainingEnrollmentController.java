package com.shootersplatform.backend.bookings.trainingenrollment.web;

import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollmentId;
import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollmentService;
import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/bookings/enrollments")
@PreAuthorize("hasRole('USER')")
class TrainingEnrollmentController {

    private final TrainingEnrollmentService trainingEnrollments;

    TrainingEnrollmentController(TrainingEnrollmentService trainingEnrollments) {
        this.trainingEnrollments = trainingEnrollments;
    }

    @GetMapping
    List<TrainingEnrollmentResponse> list(Authentication authentication) {
        return trainingEnrollments.list(currentUser(authentication).id()).stream()
                .map(TrainingEnrollmentResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TrainingEnrollmentResponse create(@Valid @RequestBody UpsertTrainingEnrollmentRequest request, Authentication authentication) {
        return TrainingEnrollmentResponse.from(trainingEnrollments.create(
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
            @Valid @RequestBody UpsertTrainingEnrollmentRequest request,
            Authentication authentication
    ) {
        return TrainingEnrollmentResponse.from(trainingEnrollments.update(
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
