// src/main/java/at/fhtw/usageworker/service/UsageListener.java
package at.fhtw.usageworker.service;

import at.fhtw.usageworker.model.ProductionEvent;
import at.fhtw.usageworker.model.UsageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsageListener {

    private final UsageService usageService;

    /** USER-Events (Verbrauch). Erwartet: communityUsed, gridUsed (ignoriert), timestamp. */
    @RabbitListener(queues = "#{@usedQueue.name}")
    public void onUsed(@Payload UsageEvent evt) {
        Instant ts = evt.getTimestamp();  // <-- Getter (POJO)
        if (ts == null) {
            log.warn("⚠️ USER event without timestamp -> ignoring: {}", evt);
            return;
        }
        double used = evt.getCommunityUsed(); // <-- Getter (POJO)
        log.info("⚡ received used={}, grid={}, ts={}", used, evt.getGridUsed(), ts);

        usageService.processUsage(ts, used);
    }

    /** PRODUCER-Events (Produktion). Erwartet: producedKwh, timestamp, producerId, sourceType. */
    @RabbitListener(queues = "#{@producedQueue.name}")
    public void onProduced(@Payload ProductionEvent evt) {
        Instant ts = evt.getTimestamp();        // <-- Getter (POJO)
        if (ts == null) {
            log.warn("⚠️ PRODUCER event without timestamp -> ignoring: {}", evt);
            return;
        }

        double produced = evt.getProducedKwh(); // <-- Getter (POJO)
        log.info("☀️ received production from producer={}, source={}, kWh={}, ts={}",
                evt.getProducerId(),            // <-- Getter (POJO)
                evt.getSourceType(),            // <-- Getter (POJO)
                produced, ts);

        usageService.processProduction(ts, produced);
    }
}
