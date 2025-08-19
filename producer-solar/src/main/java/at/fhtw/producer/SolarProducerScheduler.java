package at.fhtw.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class SolarProducerScheduler {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routing;

    public SolarProducerScheduler(
            RabbitTemplate rabbitTemplate,
            @Value("${app.exchange}") String exchange,
            @Value("${app.routing}") String routing) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routing = routing;
    }

    @Scheduled(fixedRate = 3000, initialDelay = 2000)
    public void sendProduction() {
        double base = 2.0;
        int hour = LocalDateTime.now().getHour();
        double sunFactor = (hour >= 8 && hour <= 18) ? 1.0 : 0.2; // simple day/night factor
        double kwh = Math.round((base * sunFactor + Math.random()) * 100.0) / 100.0;

        Map<String, Object> msg = Map.of(
                "type", "production",
                "kwh", kwh,
                "timestamp", LocalDateTime.now().toString()
        );

        rabbitTemplate.convertAndSend(exchange, routing, msg);
    }
}
