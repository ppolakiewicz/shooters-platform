package com.shootersplatform.backend.bookings.web;

import com.shootersplatform.backend.bookings.location.domain.LocationValidationException;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationNotFoundException;
import com.shootersplatform.backend.bookings.reservation.domain.ReservationValidationException;
import com.shootersplatform.backend.bookings.term.domain.TermNotFoundException;
import com.shootersplatform.backend.bookings.term.domain.TermValidationException;
import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplateNotFoundException;
import com.shootersplatform.backend.bookings.trainingtemplate.domain.TrainingTemplateValidationException;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistNotFoundException;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
class BookingExceptionHandler {

    @ExceptionHandler({
            TermNotFoundException.class,
            ReservationNotFoundException.class,
            WaitlistNotFoundException.class,
            TrainingTemplateNotFoundException.class
    })
    ProblemDetail notFound(RuntimeException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Booking resource not found");
        return problem;
    }

    @ExceptionHandler({
            TermValidationException.class,
            ReservationValidationException.class,
            WaitlistValidationException.class,
            TrainingTemplateValidationException.class,
            LocationValidationException.class
    })
    ProblemDetail validation(RuntimeException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid booking request");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail requestValidation(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        problem.setTitle("Invalid request");
        List<Map<String, String>> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", Objects.toString(error.getDefaultMessage(), "")
                ))
                .toList();
        problem.setProperty("errors", errors);
        return problem;
    }
}
