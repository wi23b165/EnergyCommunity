// src/main/java/at/fhtw/producer/service/SolarScheduler.java
package at.fhtw.producer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
public class SolarScheduler {

    private final SolarProducerService service;

    public SolarScheduler(SolarProducerService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "#{T(java.util.concurrent.ThreadLocalRandom).current().nextLong(1000,5001)}",
            initialDelayString = "${app.initialDelayMs:2000}")
    public void tick() {
        var evt = service.sendOneTick();
        log.info("sent produced tick: {} kWh at {}", evt.producedKwh(), evt.timestamp());
    }
}
