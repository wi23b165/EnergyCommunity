package at.fhtw.restapi.controllers;

import at.fhtw.restapi.services.EnergyService;
import at.fhtw.restapi.services.dto.DaySummaryDTO;
import at.fhtw.restapi.services.dto.EnergyUsageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usage")
@CrossOrigin(origins = "http://localhost:8080")
public class UsageController {

    private final EnergyService service;

    public UsageController(EnergyService service) {
        this.service = service;
    }

    // /usage/hourly?from=YYYY-MM-DD&to=YYYY-MM-DD   (to inclusive)
    @GetMapping("/hourly")
    public ResponseEntity<?> hourly(@RequestParam String from, @RequestParam String to) {
        LocalDate f = LocalDate.parse(from, DateTimeFormatter.ISO_DATE);
        LocalDate t = LocalDate.parse(to,   DateTimeFormatter.ISO_DATE);
        LocalDateTime a = f.atStartOfDay();
        LocalDateTime b = t.plusDays(1).atStartOfDay();
        List<EnergyUsageDTO> rows = service.getHourly(a, b);
        return ResponseEntity.ok(rows);
    }

    // /usage/summary?day=YYYY-MM-DD
    @GetMapping("/summary")
    public ResponseEntity<?> summary(@RequestParam String day) {
        try {
            LocalDate d = LocalDate.parse(day, DateTimeFormatter.ISO_DATE);
            DaySummaryDTO dto = service.getDaySummary(d);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid date. Use YYYY-MM-DD"));
        }
    }
}
