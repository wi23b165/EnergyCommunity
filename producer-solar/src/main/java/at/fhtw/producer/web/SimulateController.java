// src/main/java/at/fhtw/producer/web/SimulateController.java
package at.fhtw.producer.web;

import at.fhtw.producer.model.ProductionEvent;
import at.fhtw.producer.service.SolarProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SimulateController {

    private final SolarProducerService service;

    @RequestMapping(value = "/simulate", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> simulate(
            @RequestParam(name = "count", defaultValue = "10") int count,
            @RequestParam(name = "verbose", defaultValue = "true") boolean verbose) {

        int safe = Math.max(1, Math.min(count, 1000));
        List<ProductionEvent> sent = new ArrayList<>(safe);
        double sumProduced = 0.0;

        for (int i = 0; i < safe; i++) {
            ProductionEvent evt = service.sendOneTick();
            sent.add(evt);
            sumProduced += evt.producedKwh();
        }

        if (verbose) return ResponseEntity.ok(sent);
        return ResponseEntity.ok(Map.of(
                "ticks", safe,
                "messages", safe,
                "sumProducedKwh", Math.round(sumProduced * 100.0) / 100.0
        ));
    }
}
