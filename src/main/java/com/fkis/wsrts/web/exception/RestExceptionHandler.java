package com.fkis.wsrts.web.exception;

import static java.lang.String.join;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(value = {IllegalArgumentException.class, IllegalStateException.class})
  protected ResponseEntity<Map<String, String>> handleIllegalArgumentOrState(RuntimeException ex) {
    log.error("Illegal state or argument error occurred when handling the request", ex);
    return new ResponseEntity<>(Map.of("error", ex.getMessage()), CONFLICT);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, List<String>>> handleValidationErrors(
      MethodArgumentNotValidException ex) {
    List<String> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(fieldError -> join(": ", fieldError.getDefaultMessage(), fieldError.getField()))
        .toList();
    log.error("Validation error occurred when handling the request", ex);
    return new ResponseEntity<>(Map.of("validation_error", errors), BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGlobalException(Exception ex) {
    log.error("An internal error occurred when handling the request", ex);
    return new ResponseEntity<>(Map.of("internal_error", ex.getMessage()), INTERNAL_SERVER_ERROR);
  }
}
