package com.shootersplatform.backend.training.web;

import com.shootersplatform.backend.training.domain.TrainingNotFoundException;
import com.shootersplatform.backend.training.domain.TrainingValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
class TrainingExceptionHandler {

    @ExceptionHandler(TrainingNotFoundException.class)
    ProblemDetail trainingNotFound(TrainingNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Training not found");
        return problem;
    }

    @ExceptionHandler(TrainingValidationException.class)
    ProblemDetail trainingValidation(TrainingValidationException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid training request");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        problem.setTitle("Invalid request");
        List<Map<String, String>> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of("field", error.getField(), "message", error.getDefaultMessage()))
                .toList();
        problem.setProperty("errors", errors);
        return problem;
    }
}
