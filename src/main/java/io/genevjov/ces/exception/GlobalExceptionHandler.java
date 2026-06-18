package io.genevjov.ces.exception;

import io.genevjov.ces.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCurrencyException.class)
    ResponseEntity<ErrorResponse> handleInvalidCurrency(InvalidCurrencyException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ErrorResponse> handleValidation(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, validationMessage(ex), request);
    }

    @ExceptionHandler(CurrencyNotFoundException.class)
    ResponseEntity<ErrorResponse> handleCurrencyNotFound(CurrencyNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ExternalProviderException.class)
    ResponseEntity<ErrorResponse> handleExternalProvider(ExternalProviderException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request);
    }

    private static ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        request.getRequestURI()));
    }

    private static String validationMessage(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException validationException) {
            FieldError fieldError = validationException.getBindingResult().getFieldError();
            if (fieldError != null) {
                return fieldError.getField() + " " + fieldError.getDefaultMessage();
            }
        }
        if (ex instanceof MethodArgumentTypeMismatchException typeMismatch) {
            return "Invalid value for parameter: " + typeMismatch.getName();
        }
        return ex.getMessage();
    }
}
