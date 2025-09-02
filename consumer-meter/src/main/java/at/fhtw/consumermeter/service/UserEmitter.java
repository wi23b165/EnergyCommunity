package at.fhtw.consumermeter.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simuliert den Community Energy User:
 *  - sendet alle 1–5 Sekunden eine USER-Message
 *  - kWh je nach Tageszeit höher/niedriger
 */
@Component
@EnableScheduling
public class UserEmitter {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;
    private final Random rnd = new Random();

    public UserEmitter(RabbitTemplate rabbitTemplate,
                       @Value("${ec.exchange}") String exchange,
                       @Value("${ec.routing.used}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    // Scheduler mit zufälligem Delay zwischen 1 und 5 Sekunden
    @Scheduled(fixedDelayString = "#{T(java.util.concurrent.ThreadLocalRandom).current().nextLong(1000,5001)}")
    public void emitUsage() {
        ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        // Basisverbrauch (z. B. 0.001–0.007 kWh pro Minute)
        double base = 0.001 + rnd.nextDouble() * 0.006;

        // Tageszeit-Faktor
        double factor = peakFactor(now.getHour());
        double kwh = round3(base * factor);

        // Der Usage-Worker berechnet Grid-Anteil selbst → gridUsed = 0
        UsageMessage msg = new UsageMessage(kwh, 0.0, now.toInstant());

        rabbitTemplate.convertAndSend(exchange, routingKey, msg);
        System.out.printf("⚡ USER sent: used=%.3f grid=%.3f ts=%s%n", msg.communityUsed, msg.gridUsed, now);
    }

    private double peakFactor(int hour) {
        if ((hour >= 7 && hour <= 9) || (hour >= 18 && hour <= 21)) return 1.8; // Peak
        if (hour >= 10 && hour <= 16) return 0.9; // Tagsüber
        return 0.6; // Nacht
    }

    private static double round3(double v) { return Math.round(v * 1000.0) / 1000.0; }

    // DTO exakt so benannt, wie es der usage-worker erwartet
    public static class UsageMessage {
        public double communityUsed;
        public double gridUsed;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Instant timestamp;

        // optionale Zusatzinfos (werden ignoriert, aber schaden nicht)
        public String type = "USER";
        public String association = "COMMUNITY";
        public String communityId = "ec-01";

        public UsageMessage(double communityUsed, double gridUsed, Instant timestamp) {
            this.communityUsed = communityUsed;
            this.gridUsed = gridUsed;
            this.timestamp = timestamp;
        }
    }
}
