package com.taiwei.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns exceptions into the {@link R} envelope the UIs expect. Only exceptions the application raises on purpose
 * (or that map to a clear client error) are handled here; everything else keeps Spring Boot's default handling
 * so real server errors still surface as HTTP 5xx.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final Pattern DUPLICATE_VALUE = Pattern.compile("Duplicate entry '(.*?)' for key");

    @ExceptionHandler({DuplicateKeyException.class, SQLIntegrityConstraintViolationException.class,
            DataIntegrityViolationException.class})
    public R<String> handleIntegrityViolation(Exception ex) {
        String message = String.valueOf(ex.getMessage());
        log.warn("integrity violation: {}", message);
        Matcher matcher = DUPLICATE_VALUE.matcher(message);
        if (matcher.find()) {
            return R.error("'" + matcher.group(1) + "' already exists");
        }
        return R.error("operation conflicts with existing data");
    }

    @ExceptionHandler(CustomException.class)
    public R<String> handleCustom(CustomException ex) {
        log.warn("rejected: {}", ex.getMessage());
        return R.error(ex.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleBadRequest(Exception ex) {
        log.warn("bad request: {}", ex.getMessage());
        return R.error("malformed request");
    }
}
