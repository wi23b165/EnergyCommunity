package at.fhtw.restapi.controllers;

import at.fhtw.restapi.services.EnergyService;
import at.fhtw.restapi.services.EnergyUsageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/energy")
@CrossOrigin(origins = "http://localhost:8081") // adjust if GUI runs elsewhere
public class EnergyController {

    private final EnergyService energyService;

    public EnergyController(EnergyService energyService) {
        this.energyService = energyService;
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentEnergy() {
        try {
            EnergyUsageDTO dto = energyService.getCurrentHour();
            if (dto == null) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "INTERNAL_ERROR", "message", String.valueOf(e.getMessage())));
        }
    }

    @GetMapping("/historical")
    public ResponseEntity<?> getHistorical(
            @RequestParam("start") String start,   // <-- explicit names fix the -parameters problem
            @RequestParam("end")   String end) {

        try {
            LocalDate s = LocalDate.parse(start, DateTimeFormatter.ISO_DATE);
            LocalDate e = LocalDate.parse(end,   DateTimeFormatter.ISO_DATE);
            if (e.isBefore(s)) {
                return ResponseEntity.badRequest().body(Map.of("error", "End date must be after start date"));
            }
            // [from, to) – end exclusive so whole 'end' day is included
            LocalDateTime from = s.atStartOfDay();
            LocalDateTime to   = e.plusDays(1).atStartOfDay();

            List<EnergyUsageDTO> rows = energyService.aggregateByHour(from, to);
            return ResponseEntity.ok(rows);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format. Use YYYY-MM-DD"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "INTERNAL_ERROR", "message", String.valueOf(ex.getMessage())));
        }
    }
}
