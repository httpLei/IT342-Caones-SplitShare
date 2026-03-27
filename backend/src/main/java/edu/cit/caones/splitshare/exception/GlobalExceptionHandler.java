package edu.cit.caones.splitshare.exception;

import edu.cit.caones.splitshare.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles @Valid validation failures → 400 VALID-001
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> details = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            details.put(fe.getField(), fe.getDefaultMessage());
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("VALID-001", "Validation failed", details));
    }

    /**
     * Duplicate email on registration → 409 AUTH-002
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateEmail(
            DuplicateEmailException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail("AUTH-002", "Email already in use", ex.getMessage()));
    }

    /**
     * Wrong credentials on login → 401 AUTH-001
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCredentials(
            InvalidCredentialsException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("AUTH-001", "Invalid credentials", ex.getMessage()));
    }

            /**
             * Disabled account login attempt -> 403 AUTH-003
             */
            @ExceptionHandler(AccountDisabledException.class)
            public ResponseEntity<ApiResponse<Object>> handleAccountDisabled(
                AccountDisabledException ex) {

            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("AUTH-003", "Account is suspended", ex.getMessage()));
            }

            /**
             * Resource not found -> 404
             */
            @ExceptionHandler(NoSuchElementException.class)
            public ResponseEntity<ApiResponse<Object>> handleNotFound(
                NoSuchElementException ex) {

            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("RES-404", "Resource not found", ex.getMessage()));
            }

            /**
             * Invalid user action -> 400
             */
            @ExceptionHandler(IllegalArgumentException.class)
            public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(
                IllegalArgumentException ex) {

            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("VALID-002", "Invalid request", ex.getMessage()));
            }

            /**
             * Forbidden operation -> 403
             */
            @ExceptionHandler(AccessDeniedException.class)
            public ResponseEntity<ApiResponse<Object>> handleAccessDenied(
                AccessDeniedException ex) {

            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("AUTH-403", "Insufficient permissions", ex.getMessage()));
            }

    /**
     * Catch-all → 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("SRV-001", "An unexpected error occurred", ex.getMessage()));
    }
}
