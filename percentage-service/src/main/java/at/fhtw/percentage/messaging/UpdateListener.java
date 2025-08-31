package at.fhtw.percentage.messaging;

import at.fhtw.percentage.repo.PercentageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@Component
public class UpdateListener {

    private static final Logger log = LoggerFactory.getLogger(UpdateListener.class);

    private final PercentageRepository repo;
    private final ObjectMapper om;

    public UpdateListener(PercentageRepository repo, ObjectMapper om) {
        this.repo = repo;
        this.om = om;
    }

    /** Nimmt JSON entgegen, das zu UpdateEvent passt (Jackson macht das Mapping). */
    @RabbitListener(queues = "#{updateQueue.name}")
    public void onUpdate(UpdateEvent evt) {
        try {
            OffsetDateTime hour = parseHour(evt.hourIso());
            double used = nz(evt.communityUsed());
            double grid = nz(evt.gridUsed());

            double pct = computePercentage(used, grid);

            repo.upsert(hour, used, grid, pct);

            log.info("current_percentage upserted: hour={}, used={} kWh, grid={} kWh, pct={} %",
                    hour, used, grid, pct);
        } catch (Exception e) {
            log.error("Failed processing update message: {}", safeJson(evt), e);
        }
    }

    private OffsetDateTime parseHour(String hourIso) {
        if (hourIso == null || hourIso.isBlank()) {
            // fallback: aktuelle Stunde (UTC)
            return OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
        }
        try {
            return OffsetDateTime.parse(hourIso + ":00Z"); // wenn „YYYY-MM-DDTHH:mm“ kommt
        } catch (DateTimeParseException ignore) {
            // Versuch: Vollformat
            return OffsetDateTime.parse(hourIso).truncatedTo(ChronoUnit.HOURS);
        }
    }

    private static double nz(Double d) { return d == null ? 0.0 : d; }

    /** Anteil der Community am Verbrauch: 100 * (1 - grid/used). Begrenze auf [0,100]. */
    private static double computePercentage(double used, double grid) {
        if (used <= 0) return 0.0;
        double pct = (1.0 - (grid / used)) * 100.0;
        if (pct < 0) pct = 0;
        if (pct > 100) pct = 100;
        return Math.round(pct * 100.0) / 100.0; // auf 2 Nachkommastellen
    }

    private String safeJson(UpdateEvent e) {
        try { return om.writeValueAsString(e); } catch (Exception ex) { return "<unserializable>"; }
    }
}
