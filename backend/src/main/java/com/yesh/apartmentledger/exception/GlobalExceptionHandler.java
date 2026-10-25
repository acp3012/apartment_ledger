package com.yesh.apartmentledger.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handle 404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return formatErrorResponse("NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 2. Handle 400 Bad Request
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(BadRequestException ex) {
        return formatErrorResponse("BAD_REQUEST",ex.getMessage(),HttpStatus.BAD_REQUEST);
    }

    // 3. Handle 409 Conflict (Resource Already Exists)
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiErrorResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        return formatErrorResponse("CONFLICT",ex.getMessage(),HttpStatus.CONFLICT);
    }

    // 4. Global Fallback for anything else (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(Exception ex) {
        ex.printStackTrace(); // Keep this for your console logs
        return formatErrorResponse("Internal Server Error","An unexpected error occurred. Please try again later.",HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(err -> err.getDefaultMessage())
            .orElse("Invalid request");
        return formatErrorResponse("BAD_REQUEST", msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodValidationException(HandlerMethodValidationException ex) {
        @SuppressWarnings("null")
        String msg = ex.getAllErrors().stream()
            .map(MessageSourceResolvable::getDefaultMessage)
            .filter(message -> message != null)
            .findFirst()
            .orElse("Invalid request parameter");
        return formatErrorResponse("BAD_REQUEST", msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
            .map(violation -> violation.getMessage())
            .findFirst()
            .orElse("Invalid request");
        return formatErrorResponse("BAD_REQUEST", msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        return formatErrorResponse(
            "UNAUTHORIZED", "Authentication is required or the token is invalid.", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        return formatErrorResponse(
                "FORBIDDEN", "You are not authorized to access this resource.", HttpStatus.FORBIDDEN);
    }
    //==============================================================
    // Database level error : Catches Foreign Key failures triggered by getReferenceById()
    //==============================================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String friendlyMessage = "A database constraint was violated. Please check your inputs.";

        // 1. Get the actual database driver exception (e.g., PSQLException)
        Throwable rootCause = ex.getRootCause();

        if (rootCause != null && rootCause.getMessage() != null) {
            String dbMessage = rootCause.getMessage();

            // 2. Look for PostgreSQL's specific foreign key error pattern
            // Example: "Detail: Key (flat_id)=(999) is not present in table "flat"."
            if (dbMessage.contains("Key (")) {
                try {
                    int startIndex = dbMessage.indexOf("Key (") + 5;
                    int endIndex = dbMessage.indexOf(")", startIndex);

                    if (startIndex > 4 && endIndex > startIndex) {
                        String columnName = dbMessage.substring(startIndex, endIndex);

                        // Convert "flat_id" to "flat id" or just return it directly
                        friendlyMessage = "Invalid reference provided for field: '" + columnName + "'.";
                    }
                } catch (Exception parseException) {
                    // If parsing fails for any reason, it safely falls back to the default message
                }
            }
            // Optional: Handle unique constraint violations dynamically too
            else if (dbMessage.contains("duplicate key value")) {
                friendlyMessage = "A record with this value already exists.";
            }
        }
        return formatErrorResponse("Internal_database_error",friendlyMessage,HttpStatus.INTERNAL_SERVER_ERROR);

    }

    private ResponseEntity<ApiErrorResponse> formatErrorResponse(String errorCode, String message,HttpStatus httpstatus ){
        return new ResponseEntity<>( new ApiErrorResponse(httpstatus.value(),errorCode,message),httpstatus);
    }
}