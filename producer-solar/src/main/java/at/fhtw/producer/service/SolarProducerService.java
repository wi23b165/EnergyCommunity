// src/main/java/at/fhtw/producer/service/SolarProducerService.java
package at.fhtw.producer.service;

import at.fhtw.producer.model.ProductionEvent;
import at.fhtw.producer.model.UsageEvent;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SolarProducerService {

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange exchange;

    private final String rkProduced;
    private final String rkUsed;

    // einfache IDs via Config, damit GUI/DB später zuordnen kann
    private final String producerId;
    private final String sourceType;
    private final String communityId;

    public SolarProducerService(RabbitTemplate rabbitTemplate,
                                TopicExchange exchange,
                                @Value("${app.routing.produced:energy.produced}") String rkProduced,
                                @Value("${app.routing.used:energy.used}") String rkUsed,
                                @Value("${app.ids.producer:solar-1}") String producerId,
                                @Value("${app.ids.sourceType:SOLAR}") String sourceType,
                                @Value("${app.ids.community:ec-01}") String communityId) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.rkProduced = rkProduced;
        this.rkUsed = rkUsed;
        this.producerId = producerId;
        this.sourceType = sourceType;
        this.communityId = communityId;
    }

    /**
     * Erzeugt konsistente Produktions- und Verbrauchswerte
     * und sendet ZWEI Nachrichten:
     *   - ProductionEvent  → routingKey = app.routing.produced
     *   - UsageEvent       → routingKey = app.routing.used
     */
    public Pair sendOneTick() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        // Produktion (Solar „0..2 kWh“ pro Tick)
        double produced = round2(rnd.nextDouble(0.0, 2.0));

        // Verbrauch (Community „0..2 kWh“)
        double used = round2(rnd.nextDouble(0.0, 2.0));

        // Netzbezug nur wenn Verbrauch > Produktion
        double grid = 0.0;
        if (used > produced) {
            grid = round2(used - produced);
        }

        var now = Instant.now();

        var prodEvt = new ProductionEvent(producerId, sourceType, produced, now);
        var useEvt  = new UsageEvent(communityId, used, grid, now);

        rabbitTemplate.convertAndSend(exchange.getName(), rkProduced, prodEvt);
        rabbitTemplate.convertAndSend(exchange.getName(), rkUsed,     useEvt);

        return new Pair(prodEvt, useEvt);
    }

    private static double round2(double v){ return Math.round(v * 100.0) / 100.0; }

    // kleines Rückgabeobjekt für Logging/Controller
    public record Pair(ProductionEvent produced, UsageEvent used) {}
}
