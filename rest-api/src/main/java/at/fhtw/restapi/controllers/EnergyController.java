package at.fhtw.restapi.controllers;

import at.fhtw.restapi.services.EnergyService;
import at.fhtw.restapi.services.dto.DaySummaryDTO;
import at.fhtw.restapi.services.dto.EnergyUsageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/energy", "/api/energy"})
@CrossOrigin(origins = "http://localhost:8080")
public class EnergyController {

    private final EnergyService service;

    public EnergyController(EnergyService service) {
        this.service = service;
    }

    // /energy/current
    @GetMapping("/current")
    public ResponseEntity<?> current() {
        try {
            EnergyUsageDTO dto = service.getCurrentHour();
            return dto == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "INTERNAL_ERROR", "message", String.valueOf(e.getMessage())));
        }
    }

    // /energy/historical?start=YYYY-MM-DD&end=YYYY-MM-DD  (end inclusive)
    @GetMapping("/historical")
    public ResponseEntity<?> historical(@RequestParam("start") String start,
                                        @RequestParam("end") String end) {
        try {
            LocalDate s = LocalDate.parse(start, DateTimeFormatter.ISO_DATE);
            LocalDate e = LocalDate.parse(end, DateTimeFormatter.ISO_DATE);
            if (e.isBefore(s)) {
                return ResponseEntity.badRequest().body(Map.of("error", "End date must be after start date"));
            }
            LocalDateTime from = s.atStartOfDay();
            LocalDateTime to   = e.plusDays(1).atStartOfDay(); // exklusiv
            List<EnergyUsageDTO> rows = service.getHourly(from, to);
            return ResponseEntity.ok(rows);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid date. Use YYYY-MM-DD"));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "INTERNAL_ERROR", "message", String.valueOf(ex.getMessage())));
        }
    }
}
