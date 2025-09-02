// src/main/java/at/fhtw/producer/service/SolarProducerService.java
package at.fhtw.producer.service;

import at.fhtw.producer.model.ProductionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class SolarProducerService {

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange exchange;
    private final WeatherFactor weather;

    private final String rkProduced;
    private final String producerId;
    private final String sourceType;

    public SolarProducerService(RabbitTemplate rabbitTemplate,
                                TopicExchange exchange,
                                WeatherFactor weather,
                                @Value("${app.routing.produced:energy.produced}") String rkProduced,
                                @Value("${app.ids.producer:solar-1}") String producerId,
                                @Value("${app.ids.sourceType:SOLAR}") String sourceType) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.weather = weather;
        this.rkProduced = rkProduced;
        this.producerId = producerId;
        this.sourceType = sourceType;
    }

    /** Erzeugt einen PRODUCER-Event (kWh pro Minute) und published ihn. */
    public ProductionEvent sendOneTick() {
        var now = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        // Grundproduktion (pro Minute!)
        double base = rnd(0.0005, 0.0060);

        // Tageszeit-Faktor
        int h = now.getHour();
        double dayFactor =
                (h >= 10 && h <= 15) ? 1.8
                        : (h >= 8  && h <= 9)  ? 1.2
                        : (h >= 16 && h <= 18) ? 1.1
                        : (h >= 6  && h <= 7)  ? 0.8
                        : (h >= 19 && h <= 21) ? 0.5
                        : 0.2;

        double weatherFactor = weather.factor();  // 0.3..1.3
        double produced = round3(base * dayFactor * weatherFactor);

        var evt = new ProductionEvent(producerId, sourceType, produced, now.toInstant());
        rabbitTemplate.convertAndSend(exchange.getName(), rkProduced, evt);
        log.debug("published produced: exchange={}, rk={}, evt={}", exchange.getName(), rkProduced, evt);
        return evt;
    }

    private static double rnd(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
    private static double round3(double v){ return Math.round(v * 1000.0) / 1000.0; }
}
