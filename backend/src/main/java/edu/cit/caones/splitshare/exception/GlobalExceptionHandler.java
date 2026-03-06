package edu.cit.caones.splitshare.exception;

import edu.cit.caones.splitshare.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

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
     * Catch-all → 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("SRV-001", "An unexpected error occurred", ex.getMessage()));
    }
}
