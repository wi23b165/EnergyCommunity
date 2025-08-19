package at.fhtw.usage;

import at.fhtw.usage.domain.UsageHourly;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Component
public class UsageConsumer {

    private final UsageHourlyRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    public UsageConsumer(UsageHourlyRepository repo) {
        this.repo = repo;
    }

    // Producer messages: {"timestamp":"2025-08-18T19:03:00","produced":1.2}
    @RabbitListener(queues = "energy.produced")
    @Transactional
    public void onProduced(byte[] body) {
        try {
            Map<String, Object> msg =
                    mapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            LocalDateTime hour = toHour(msg.get("timestamp"));
            double produced = getNumber(msg, "produced");
            applyDelta(hour, produced, 0.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // User messages: {"timestamp":"2025-08-18T19:03:00","used":0.9}
    @RabbitListener(queues = "energy.used")
    @Transactional
    public void onUsed(byte[] body) {
        try {
            Map<String, Object> msg =
                    mapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            LocalDateTime hour = toHour(msg.get("timestamp"));
            double used = msg.containsKey("used")
                    ? getNumber(msg, "used")
                    : getNumber(msg, "consumed"); // alternate key
            applyDelta(hour, 0.0, used);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LocalDateTime toHour(Object ts) {
        try {
            if (ts instanceof String s) {
                return LocalDateTime.parse(s).truncatedTo(ChronoUnit.HOURS);
            }
        } catch (DateTimeParseException ignored) {}
        return LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
    }

    private double getNumber(Map<String, Object> msg, String key) {
        Object v = msg.getOrDefault(key, 0);
        return (v instanceof Number n) ? n.doubleValue() : 0.0;
    }

    // Must be public for Spring proxies when used with @Transactional
    @Transactional
    public void applyDelta(LocalDateTime hour, double producedDelta, double usedDelta) {
        UsageHourly row = repo.findById(hour).orElse(new UsageHourly(hour, 0, 0, 0));

        row.setCommunityProduced(row.getCommunityProduced() + producedDelta);
        row.setCommunityUsed(row.getCommunityUsed() + usedDelta);

        double grid = Math.max(0.0, row.getCommunityUsed() - row.getCommunityProduced());
        row.setGridUsed(grid);

        repo.save(row);
    }
}
