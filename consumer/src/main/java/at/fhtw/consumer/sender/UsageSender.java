package at.fhtw.consumer.sender;

import at.fhtw.consumer.dto.EnergyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.concurrent.*;

@Component
public class UsageSender implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsageSender.class);
    private final RabbitTemplate rabbit;
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    @Value("${app.exchange:energy.events}") private String exchange;
    @Value("${app.routing:energy.usage}")  private String routingKey;
    @Value("${user.power.minKw:0.2}")      private double minKw;
    @Value("${user.power.maxKw:5.0}")      private double maxKw;

    public UsageSender(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("UsageSender started → exchange='{}', routingKey='{}'", exchange, routingKey);
        scheduleNext();
    }

    private void scheduleNext() {
        long delaySec = ThreadLocalRandom.current().nextLong(1, 6); // 1..5 s
        exec.schedule(() -> {
            try {
                double kw  = ThreadLocalRandom.current().nextDouble(minKw, maxKw);
                double kwh = round(kw * (delaySec / 3600.0), 4);
                var msg = new EnergyMessage("USAGE", kwh, "user-1", OffsetDateTime.now());
                rabbit.convertAndSend(exchange, routingKey, msg);
                log.debug("Sent USAGE: kw≈{}, dt={}s, kwh={}, at={}",
                        round(kw, 3), delaySec, kwh, msg.recordedAt());
            } catch (Exception ex) {
                log.warn("Failed to publish usage message: {}", ex.getMessage());
            } finally {
                scheduleNext(); // wieder planen
            }
        }, delaySec, TimeUnit.SECONDS);
    }

    private static double round(double v, int scale) {
        return BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    @PreDestroy
    public void shutdown() {
        exec.shutdownNow();
    }
}
