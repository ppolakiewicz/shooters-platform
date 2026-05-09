package com.shootersplatform.backend.bookings.term.web;

import com.shootersplatform.backend.bookings.location.domain.LocationValidationException;
import com.shootersplatform.backend.bookings.term.domain.TermNotFoundException;
import com.shootersplatform.backend.bookings.term.domain.TermValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class TermExceptionHandler {

    @ExceptionHandler(TermNotFoundException.class)
    ProblemDetail notFound(TermNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Term not found");
        return problem;
    }

    @ExceptionHandler({TermValidationException.class, LocationValidationException.class})
    ProblemDetail validation(RuntimeException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid term request");
        return problem;
    }
}
