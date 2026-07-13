package ru.practicum.main.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        return new ApiError(
                List.of(e.getMessage()),
                e.getMessage(),
                "The required object was not found.",
                HttpStatus.NOT_FOUND.name(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException e) {
        return new ApiError(
                List.of(e.getMessage()),
                e.getMessage(),
                "For the requested operation the conditions are not met.",
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleRuntimeException(final RuntimeException e) {
        String message = e.getMessage();
        if (message != null && (message.contains("not found") || message.contains("Not found"))) {
            return new ApiError(
                    List.of(message),
                    message,
                    "The required object was not found.",
                    HttpStatus.NOT_FOUND.name(),
                    LocalDateTime.now()
            );
        }
        return new ApiError(
                List.of(message),
                message,
                "For the requested operation the conditions are not met.",
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        return new ApiError(
                List.of(e.getMessage()),
                "Integrity constraint has been violated.",
                "Integrity constraint has been violated.",
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> String.format("Field: %s. Error: %s. Value: %s",
                        error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                .toList();

        return new ApiError(
                errors,
                errors.get(0),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        return new ApiError(
                List.of(e.getMessage()),
                String.format("Failed to convert value of type '%s' to required type '%s'",
                        e.getValue() != null ? e.getValue().getClass().getSimpleName() : "null",
                        e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown"),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Exception e) {
        return new ApiError(
                List.of(e.getMessage()),
                e.getMessage(),
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                LocalDateTime.now()
        );
    }
}