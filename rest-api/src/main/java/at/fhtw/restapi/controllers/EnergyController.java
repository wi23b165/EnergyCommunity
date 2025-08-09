package at.fhtw.restapi.controllers;

import at.fhtw.restapi.dto.EnergyUsageDTO;
import at.fhtw.restapi.services.EnergyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    private final EnergyService energyService;
    private final DateTimeFormatter isoDate = DateTimeFormatter.ISO_DATE;

    public EnergyController(EnergyService energyService) {
        this.energyService = energyService;
    }

    /**
     * Returns the aggregated usage for the current hour based on DB data.
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentEnergy() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        return energyService.aggregateByHour(start, end)
                .stream()
                .findFirst()
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * Aggregates historical data by hour between start and end (inclusive).
     * Query params must be in YYYY-MM-DD format.
     */
    @GetMapping("/historical")
    public ResponseEntity<?> getHistoricalData(@RequestParam String start,
                                               @RequestParam String end) {
        try {
            LocalDate startDate = LocalDate.parse(start, isoDate);
            LocalDate endDate = LocalDate.parse(end, isoDate);
            if (endDate.isBefore(startDate)) {
                return ResponseEntity.badRequest().body(
                        java.util.Map.of("error", "End date must be after start date"));
            }

            LocalDateTime from = startDate.atStartOfDay();
            LocalDateTime to = endDate.plusDays(1).atStartOfDay(); // exclusive upper bound

            List<EnergyUsageDTO> result = energyService.aggregateByHour(from, to);
            return ResponseEntity.ok(result);

        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "Invalid date format. Use YYYY-MM-DD"));
        }
    }
}