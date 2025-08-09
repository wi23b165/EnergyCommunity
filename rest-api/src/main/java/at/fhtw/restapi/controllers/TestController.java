package at.fhtw.restapi.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/test-date")
    public Map<String, Object> testDate(@RequestParam LocalDate date) {
        return Map.of("received", date);
    }
}