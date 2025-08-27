// src/main/java/at/fhtw/producer/web/SimulateController.java
package at.fhtw.producer.web;

import at.fhtw.producer.model.TickResult;
import at.fhtw.producer.service.SolarProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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

    /** /api/simulate?count=100 – schickt count Ticks (je 2 Messages) und gibt die Ticks zurück. */
    // SimulateController – alternative Response
    @RequestMapping(value = "/simulate", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> simulate(
            @RequestParam(name = "count", defaultValue = "10") int count,
            @RequestParam(name = "verbose", defaultValue = "true") boolean verbose) {

        int safe = Math.max(1, Math.min(count, 1000));
        var sent = new ArrayList<TickResult>(safe);
        double sumProduced = 0, sumUsed = 0, sumGrid = 0;

        for (int i = 0; i < safe; i++) {
            var pair = service.sendOneTick();
            sent.add(new TickResult(pair.produced(), pair.used()));
            sumProduced += pair.produced().producedKwh();
            sumUsed     += pair.used().communityUsed();
            sumGrid     += pair.used().gridUsed();
        }
        if (verbose) return ResponseEntity.ok(sent);
        return ResponseEntity.ok(Map.of(
                "ticks", safe,
                "messages", safe * 2,
                "sumProducedKwh", Math.round(sumProduced*100)/100.0,
                "sumUsedKwh", Math.round(sumUsed*100)/100.0,
                "sumGridKwh", Math.round(sumGrid*100)/100.0
        ));
    }

}
