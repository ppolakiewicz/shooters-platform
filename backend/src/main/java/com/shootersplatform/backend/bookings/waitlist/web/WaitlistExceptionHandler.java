package com.shootersplatform.backend.bookings.waitlist.web;

import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistNotFoundException;
import com.shootersplatform.backend.bookings.waitlist.domain.WaitlistValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class WaitlistExceptionHandler {

    @ExceptionHandler(WaitlistNotFoundException.class)
    ProblemDetail notFound(WaitlistNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Booking resource not found");
        return problem;
    }

    @ExceptionHandler(WaitlistValidationException.class)
    ProblemDetail validation(WaitlistValidationException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid booking request");
        return problem;
    }
}
