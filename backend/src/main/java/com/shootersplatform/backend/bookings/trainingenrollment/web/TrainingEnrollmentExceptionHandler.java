package com.shootersplatform.backend.bookings.trainingenrollment.web;

import com.shootersplatform.backend.bookings.location.domain.LocationValidationException;
import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollmentNotFoundException;
import com.shootersplatform.backend.bookings.trainingenrollment.domain.TrainingEnrollmentValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@SuppressWarnings("unused")
class TrainingEnrollmentExceptionHandler {

    @ExceptionHandler(TrainingEnrollmentNotFoundException.class)
    ProblemDetail notFound(TrainingEnrollmentNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Training enrollment not found");
        return problem;
    }

    @ExceptionHandler({TrainingEnrollmentValidationException.class, LocationValidationException.class})
    ProblemDetail validation(RuntimeException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid training enrollment request");
        return problem;
    }
}
