package com.emis.academicservice.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String STATUS = "status";
    private static final String MESSAGE = "message";
    private static final String FIELD_ERRORS = "fieldErrors";
    private static final String ERROR = "error";
    private static final String TIMESTAMP = "timestamp";
    private static final String BAD_REQUEST = "Bad Request";
    private static final String FIELD = "field";


    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationException(WebExchangeBindException ex) {
        log.error("Validation error: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
            log.error("Field '{}' validation failed: {}", fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());
        response.put(ERROR, "Validation Failed");
        response.put(MESSAGE, "Input validation failed");
        response.put(FIELD_ERRORS, fieldErrors);

        return response;
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<Map<String, Object>> handleServerWebInputException(ServerWebInputException ex) {
        log.error("Failed to read HTTP message: ", ex);

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());
        response.put(ERROR, BAD_REQUEST);

        // Extract detailed error information
        Throwable cause = ex.getCause();
        switch(cause){
            case InvalidFormatException ife -> {
                String fieldName = ife.getPath().stream()
                        .map(JsonMappingException.Reference::getFieldName)
                        .collect(Collectors.joining("."));

                String targetType = ife.getTargetType().getSimpleName();
                Object value = ife.getValue();

                response.put(MESSAGE, String.format(
                        "Invalid value '%s' for field '%s'. Expected type: %s",
                        value, fieldName, targetType
                ));
                response.put(FIELD, fieldName);
                response.put("rejectedValue", value);
                response.put("expectedType", targetType);

                log.error("Invalid format for field '{}': expected {}, got '{}'",
                        fieldName, targetType, value);
            }
            case MismatchedInputException mie -> {
                String fieldName = mie.getPath().stream()
                        .map(JsonMappingException.Reference::getFieldName)
                        .collect(Collectors.joining("."));

                response.put(MESSAGE, String.format(
                        "Missing or invalid value for required field '%s'", fieldName
                ));
                response.put(FIELD, fieldName);

                log.error("Mismatched input for field '{}': {}", fieldName, mie.getMessage());
            }
            default -> {
                response.put(MESSAGE, "Failed to read request body: " + ex.getReason());
                log.error("Failed to parse request body: {}", ex.getReason());
            }
        }

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ArgumentIsNullException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleArgumentIsNull(ArgumentIsNullException ex) {
        log.error("Argument is null: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());
        response.put(ERROR, BAD_REQUEST);
        response.put(MESSAGE, ex.getMessage());

        if (ex.getFieldName() != null) {
            response.put(FIELD, ex.getFieldName());
        }

        return response;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException ex) {

        Throwable root = ex.getCause();
        String message = ex.getMessage();

        while (root != null) {
            if (root instanceof IllegalArgumentException) {
                message = root.getMessage();
                break;
            }
            root = root.getCause();
        }


        log.error("Invalid argument: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());
        response.put(ERROR, BAD_REQUEST);
        response.put(MESSAGE, message);

        return response;
    }

    @ExceptionHandler(SchoolNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleSchoolNotFound(SchoolNotFoundException ex) {
        log.error("School not found: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.NOT_FOUND.value());
        response.put(ERROR, "Not Found");
        response.put(MESSAGE, ex.getMessage());

        // Add field information if available
        if (ex.getFieldName() != null) {
            response.put(FIELD, ex.getFieldName());
        }
        if (ex.getSearchValue() != null) {
            response.put("searchValue", ex.getSearchValue());
        }
        return response;
    }

    @ExceptionHandler(AcademicServiceFailureException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleAcademicServiceFailure(AcademicServiceFailureException ex) {
        log.error("School not found: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.NOT_FOUND.value());
        response.put(ERROR, "Not Found");
        response.put(MESSAGE, ex.getMessage());

        // Add field information if available
        if (ex.getFieldName() != null) {
            response.put(FIELD, ex.getFieldName());
        }
        if (ex.getFieldName() != null) {
            response.put("fieldName", ex.getFieldName());
        }
        return response;
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, Object> handleSchoolServiceDown(ServiceUnavailableException ex) {
        log.error("School service is down: ", ex);

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put(ERROR, "Service Unavailable");
        response.put(MESSAGE, ex.getMessage());

        return response;
    }

    @ExceptionHandler(DuplicateClassException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleDuplicateClassException(DuplicateClassException ex) {
        log.error("Class creation failure: ", ex);

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.CONFLICT.value());
        response.put(ERROR, "Conflic");
        response.put(MESSAGE, ex.getMessage());

        return response;
    }

    @ExceptionHandler(ClassSectionCreationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleClassSectionException(ClassSectionCreationException ex) {
        log.error("Class creation failure: ", ex);

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());
        response.put(ERROR, "Bad Request");
        response.put(MESSAGE, ex.getMessage());

        return response;
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationException(ValidationException ex) {
        log.error("Subject creation failure: ", ex);

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());
        response.put(ERROR, "BadRequest");
        response.put(MESSAGE, ex.getMessage());

        return response;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: ", ex);

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.NOT_FOUND.value());
        response.put(ERROR, "Resource Not found");
        response.put(MESSAGE, ex.getMessage());

        return response;
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        log.error("Resource already exists: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());
        response.put(ERROR, BAD_REQUEST);
        response.put(MESSAGE, ex.getMessage());

        if (ex.getFieldName() != null) {
            response.put(FIELD, ex.getFieldName());
        }
        if (ex.getRejectedValue() != null) {
            response.put("rejectedValue", ex.getRejectedValue());
        }

        return response;
    }

}
