package at.fhtw.usageworker.service;

import at.fhtw.usageworker.dto.UpdateEvent;
import at.fhtw.usageworker.model.UsageEvent;
import at.fhtw.usageworker.model.UsageHourly;
import at.fhtw.usageworker.repo.UsageHourlyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsageService {

    private final UsageHourlyRepository usageRepo;
    private final RabbitTemplate rabbitTemplate;

    @Value("${ec.exchange}") private String exchange;
    @Value("${ec.routing.update}") private String rkUpdate;

    /** Listener-kompatibel: aggregiert bereits vorbereitete Dreifach-Werte (produced/used/grid). */
    @Transactional
    public void apply(UsageEvent evt) {
        LocalDateTime hour = truncateToHourUtc(evt.getTimestamp());
        UsageHourly row = usageRepo.findById(hour)
                .orElseGet(() -> new UsageHourly(hour, 0d, 0d, 0d));

        row.setCommunityUsed  (round3(row.getCommunityUsed()   + evt.getCommunityUsed()));
        row.setGridUsed       (round3(row.getGridUsed()        + evt.getGridUsed()));
        row.setCommunityProduced(round3(row.getCommunityProduced() + evt.getCommunityProduced()));

        usageRepo.save(row);
        publish(row);
        log.info("✅ aggregated (apply) hour={}, produced={}, used={}, grid={}",
                hour, row.getCommunityProduced(), row.getCommunityUsed(), row.getGridUsed());
    }

    /** Verarbeitet einen reinen VERBRAUCHS-Event (kWh) und berechnet Grid-Anteil gegen den aktuellen Produktionsstand. */
    @Transactional
    public void processUsage(Instant tsUtc, double kwh) {
        LocalDateTime hour = truncateToHourUtc(tsUtc);
        UsageHourly row = usageRepo.findById(hour)
                .orElseGet(() -> new UsageHourly(hour, 0d, 0d, 0d));

        double newUsed = row.getCommunityUsed() + kwh;

        // Grid = Überschuss des Verbrauchs über die Community-Produktion
        double newGrid = 0d;
        if (newUsed > row.getCommunityProduced()) {
            newGrid = round3(newUsed - row.getCommunityProduced());
        }

        row.setCommunityUsed(round3(newUsed));
        row.setGridUsed(newGrid);

        usageRepo.save(row);
        publish(row);
        log.info("✅ aggregated (usage) hour={}, produced={}, used={}, grid={}",
                hour, row.getCommunityProduced(), row.getCommunityUsed(), row.getGridUsed());
    }

    /** Verarbeitet einen reinen PRODUKTIONS-Event (kWh). */
    @Transactional
    public void processProduction(Instant tsUtc, double kwh) {
        LocalDateTime hour = truncateToHourUtc(tsUtc);
        UsageHourly row = usageRepo.findById(hour)
                .orElseGet(() -> new UsageHourly(hour, 0d, 0d, 0d));

        row.setCommunityProduced(round3(row.getCommunityProduced() + kwh));

        // Wenn Produktion gestiegen ist, könnte der bisherige Grid-Überschuss teilweise verschwinden.
        // In unserem Modell bleibt grid_used = max(0, used - produced) für die Stunde.
        double recomputedGrid = 0d;
        if (row.getCommunityUsed() > row.getCommunityProduced()) {
            recomputedGrid = round3(row.getCommunityUsed() - row.getCommunityProduced());
        }
        row.setGridUsed(recomputedGrid);

        usageRepo.save(row);
        publish(row);
        log.info("✅ aggregated (production) hour={}, produced={}, used={}, grid={}",
                hour, row.getCommunityProduced(), row.getCommunityUsed(), row.getGridUsed());
    }

    /* ---------- Helpers ---------- */

    private static LocalDateTime truncateToHourUtc(Instant ts) {
        return ts.atOffset(ZoneOffset.UTC).withMinute(0).withSecond(0).withNano(0).toLocalDateTime();
    }

    private static double round3(double v) {
        return Math.round(v * 1000d) / 1000d;
    }

    private void publish(UsageHourly row) {
        String hourIso = row.getHour().atOffset(ZoneOffset.UTC).toString(); // z.B. 2025-01-10T14:00Z
        UpdateEvent evt = new UpdateEvent(
                hourIso,
                row.getCommunityProduced(),
                row.getCommunityUsed(),
                row.getGridUsed()
        );
        rabbitTemplate.convertAndSend(exchange, rkUpdate, evt);
        log.debug("↗ published UpdateEvent to {}:{} -> {}", exchange, rkUpdate, evt);
    }
}
