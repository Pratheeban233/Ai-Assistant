package com.ai.prathi.controller;

import dev.langchain4j.exception.LangChain4jException;
import com.ai.prathi.service.ApprovalConflictException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(LangChain4jException.class)
    public ResponseEntity<ProblemDetail> handleAiProviderError(
            LangChain4jException exception, HttpServletRequest request) {
        log.error("AI provider request failed for {}", request.getRequestURI(), exception);
        return problem(
                HttpStatus.BAD_GATEWAY,
                "AI_PROVIDER_ERROR",
                "The AI provider could not complete the request. Please try again.",
                request
        );
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ProblemDetail> handleBadRequest(
            Exception exception, HttpServletRequest request) {
        String detail = exception instanceof HttpMessageNotReadableException
                ? "The request body is missing, malformed, or invalid."
                : exception.getMessage();
        return problem(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                detail,
                request
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleResponseStatus(
            ResponseStatusException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String code = status == HttpStatus.UNAUTHORIZED
                ? "APPROVAL_UNAUTHORIZED"
                : status == HttpStatus.SERVICE_UNAVAILABLE
                        ? "APPROVAL_REVIEW_DISABLED"
                        : "REQUEST_REJECTED";
        String detail = exception.getReason() == null
                ? status.getReasonPhrase()
                : exception.getReason();
        return problem(status, code, detail, request);
    }

    @ExceptionHandler(ApprovalConflictException.class)
    public ResponseEntity<ProblemDetail> handleApprovalConflict(
            ApprovalConflictException exception, HttpServletRequest request) {
        return problem(
                HttpStatus.CONFLICT,
                "APPROVAL_ALREADY_REVIEWED",
                exception.getMessage(),
                request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationError(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .reduce((first, next) -> first + "; " + next)
                .orElse("The request contains invalid fields.");
        return problem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", detail, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedError(
            Exception exception, HttpServletRequest request) {
        log.error("Unexpected request failure for {}", request.getRequestURI(), exception);
        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again.",
                request
        );
    }

    private ResponseEntity<ProblemDetail> problem(
            HttpStatus status, String code, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", code);
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).body(problem);
    }
}
