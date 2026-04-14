package com.example.paymentservice.exception.handler;

import com.example.paymentservice.dto.error.ErrorResponse;
import com.example.paymentservice.exception.payment.IdempotencyConflictException;
import com.example.paymentservice.exception.payment.InvalidPaymentStateTransitionException;
import com.example.paymentservice.exception.payment.PaymentNotFoundException;
import com.example.paymentservice.exception.payment.RetryNotAllowedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = buildErrorResponse("INVALID_REQUEST", ex.getMessage(), null, null, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream().findFirst().map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage()).orElse("Validation failed");

        ErrorResponse errorResponse = buildErrorResponse("INVALID_REQUEST", message, null, null, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = buildErrorResponse("INVALID_REQUEST", ex.getMessage(), null, null, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ErrorResponse errorResponse = buildErrorResponse(status.name(), ex.getReason(), null, null, request);

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), ex);

        ErrorResponse errorResponse = buildErrorResponse("INTERNAL_ERROR", "Something went wrong", null, null, request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyConflictException(IdempotencyConflictException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = buildErrorResponse("IDEMPOTENCY_CONFLICT", ex.getMessage(), null, null, request);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(InvalidPaymentStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentStateTransitionException(InvalidPaymentStateTransitionException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = buildErrorResponse("INVALID_STATE_TRANSITION", ex.getMessage(), null, null, request);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    @ExceptionHandler(RetryNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleRetryNotAllowed(RetryNotAllowedException ex, HttpServletRequest request) {
        ErrorResponse error = buildErrorResponse("RETRY_NOT_ALLOWED", ex.getMessage(), null, null, request);

        return ResponseEntity.unprocessableEntity().body(error);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = buildErrorResponse("PAYMENT_NOT_FOUND", ex.getMessage(), null, null, request);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    private ErrorResponse buildErrorResponse(String errorCode, String errorMessage, String paymentId, String status, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(errorCode);
        errorResponse.setErrorMessage(errorMessage);
        errorResponse.setPaymentId(paymentId);
        errorResponse.setStatus(status);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setRequestId(request.getHeader("X-Request-Id"));

        return errorResponse;
    }
}
