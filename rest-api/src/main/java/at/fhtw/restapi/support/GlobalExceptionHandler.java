package at.fhtw.restapi.support;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> onAny(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(Map.of("error","INTERNAL_ERROR","message", ex.getMessage()));
    }
}
