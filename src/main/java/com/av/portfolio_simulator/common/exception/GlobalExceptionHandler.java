package com.av.portfolio_simulator.common.exception;

import com.av.portfolio_simulator.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for all controllers.
 * Maps each exception type to the appropriate HTTP status code
 * and returns a consistent ApiResponse error body.
 *
 * HTTP status mapping:
 * 400 Bad Request     — validation errors (@Valid)
 * 401 Unauthorized    — invalid credentials
 * 404 Not Found       — resource does not exist
 * 409 Conflict        — business rule violation
 * 503 Unavailable     — external market data API failure
 * 500 Internal Error  — unexpected errors
 */
@SuppressWarnings("unused")
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles @Valid validation failures.
     * Collects all field errors into a single readable message.
     * Example: "username: Username is required; email: Invalid email format"
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    /**
     * Handles bad credentials specifically.
     * BadCredentialsException is a subclass of AuthenticationException
     * but needs explicit handling in Spring Security 7.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password"));
    }


    /**
     * Handles invalid credentials and other Spring Security authentication failures.
     * Returns 401 instead of exposing internal error details.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password"));
    }

    /**
     * Handles resource not found errors.
     * Returns 404 when a portfolio, competition, or symbol doesn't exist.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles business rule violations.
     * Returns 409 for conflicts like insufficient balance or duplicate names.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles external market data API failures.
     * Returns 503 when Alpha Vantage is unavailable or rate limited.
     */
    @ExceptionHandler(MarketDataException.class)
    public ResponseEntity<ApiResponse<Void>> handleMarketDataException(MarketDataException ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(ex.getMessage()));
    }


    /**
     * Catch-all handler for any unhandled exceptions.
     * Prevents internal error details from leaking to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred"));
    }

}
