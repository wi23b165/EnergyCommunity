package at.fhtw.restapi.controllers;

import at.fhtw.restapi.entities.EnergyReading;
import at.fhtw.restapi.repositories.EnergyReadingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/ingress")
public class IngressController {
    private final EnergyReadingRepository repo;
    public IngressController(EnergyReadingRepository repo) { this.repo = repo; }

    @PostMapping("/reading")
    public ResponseEntity<?> post(@RequestBody Map<String, Object> body) {
        LocalDateTime ts = LocalDateTime.parse((String) body.getOrDefault("timestamp", LocalDateTime.now().toString()));
        double produced = ((Number) body.getOrDefault("communityProduced", 0)).doubleValue();
        double used = ((Number) body.getOrDefault("communityUsed", 0)).doubleValue();
        double grid = ((Number) body.getOrDefault("gridUsed", 0)).doubleValue();

        EnergyReading r = new EnergyReading(ts, produced, used, grid);
        return ResponseEntity.ok(repo.save(r));
    }
}