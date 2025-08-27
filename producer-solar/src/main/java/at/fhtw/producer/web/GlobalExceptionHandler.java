// src/main/java/at/fhtw/producer/web/GlobalExceptionHandler.java
package at.fhtw.producer.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> onAny(Exception ex) {
        log.error("API error", ex);
        return ResponseEntity
                .internalServerError()
                .body(Map.of(
                        "error", ex.getClass().getSimpleName(),
                        "message", ex.getMessage() == null ? "(no message)" : ex.getMessage()
                ));
    }
}
