// src/main/java/at/fhtw/producer/service/SolarScheduler.java
package at.fhtw.producer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SolarScheduler {

    private final SolarProducerService service;

    public SolarScheduler(SolarProducerService service) {
        this.service = service;
    }

    @Scheduled(fixedRateString = "${app.fixedRateMs:5000}", initialDelayString = "${app.initialDelayMs:2000}")
    public void tick() {
        var pair = service.sendOneTick();
        log.info("sent produced={}, used={}", pair.produced(), pair.used());
    }
}
