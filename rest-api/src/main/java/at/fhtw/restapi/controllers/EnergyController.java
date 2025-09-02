package at.fhtw.restapi.controllers;

import at.fhtw.restapi.dto.CurrentPercentageDto;
import at.fhtw.restapi.dto.UsageHourlyDto;
import at.fhtw.restapi.services.EnergyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/energy", "/api/energy"})
@CrossOrigin(origins = "http://localhost:8080")
@RequiredArgsConstructor
public class EnergyController {

    private final EnergyService service;

    /** GET /energy/current – liefert die Prozentwerte der aktuellsten Stunde. */
    @GetMapping("/current")
    public ResponseEntity<?> current() {
        try {
            return service.getCurrent()
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.noContent().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", String.valueOf(e.getMessage())
            ));
        }
    }

    /**
     * GET /energy/historical?start=YYYY-MM-DD&end=YYYY-MM-DD
     * 'end' inklusiv – intern bis (end + 1T) exklusiv.
     */
    @GetMapping({"/historical", "/history"})
    public ResponseEntity<?> historical(@RequestParam("start") LocalDate start,
                                        @RequestParam("end") LocalDate end) {
        try {
            if (end.isBefore(start)) {
                return ResponseEntity.badRequest().body(Map.of("error", "End date must be after start date"));
            }
            // [start, end+1d) in UTC
            var from = start.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
            var to   = end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();

            List<UsageHourlyDto> rows = service.getHistorical(from, to);
            return ResponseEntity.ok(rows);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", String.valueOf(ex.getMessage())
            ));
        }
    }
}
