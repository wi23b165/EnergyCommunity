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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsageService {

    private final UsageHourlyRepository usageRepo;
    private final RabbitTemplate rabbitTemplate;

    @Value("${ec.exchange}")        private String exchange;
    @Value("${ec.routing.update}")  private String rkUpdate;

    /**
     * Aggregiert einen kombinierten Event (produced/used).
     * Grid wird DB-seitig neu berechnet: max(used - produced, 0).
     */
    @Transactional
    public void apply(UsageEvent evt) {
        LocalDateTime hour = truncateToHourUtc(evt.getTimestamp());

        // Optional: Deltas runden wie bisher (falls gewünscht)
        double producedDelta = round3(evt.getCommunityProduced());
        double usedDelta     = round3(evt.getCommunityUsed());

        usageRepo.upsertDeltaRecomputeGrid(hour, producedDelta, usedDelta);

        // Für Publish aktuellen Stand lesen
        usageRepo.findById(hour).ifPresent(this::publish);

        log.info("✅ aggregated (apply) hour={}, +produced={}, +used={}",
                hour, producedDelta, usedDelta);
    }

    /**
     * Reiner VERBRAUCHS-Event (kWh). Grid wird DB-seitig neu berechnet.
     */
    @Transactional
    public void processUsage(Instant tsUtc, double kwh) {
        LocalDateTime hour = truncateToHourUtc(tsUtc);

        double usedDelta = round3(kwh);
        usageRepo.upsertDeltaRecomputeGrid(hour, 0.0, usedDelta);

        usageRepo.findById(hour).ifPresent(this::publish);
        log.info("✅ aggregated (usage) hour={}, +used={}", hour, usedDelta);
    }

    /**
     * Reiner PRODUKTIONS-Event (kWh). Grid wird DB-seitig neu berechnet.
     */
    @Transactional
    public void processProduction(Instant tsUtc, double kwh) {
        LocalDateTime hour = truncateToHourUtc(tsUtc);

        double producedDelta = round3(kwh);
        usageRepo.upsertDeltaRecomputeGrid(hour, producedDelta, 0.0);

        usageRepo.findById(hour).ifPresent(this::publish);
        log.info("✅ aggregated (production) hour={}, +produced={}", hour, producedDelta);
    }

    /* ---------- Helpers ---------- */

    /** Trunkiert einen Zeitstempel auf Stundenbeginn in UTC (z. B. 17:13 → 17:00). */
    private static LocalDateTime truncateToHourUtc(Instant ts) {
        return ts.atOffset(ZoneOffset.UTC)
                .truncatedTo(ChronoUnit.HOURS)
                .toLocalDateTime();
    }

    /** Rundet auf drei Nachkommastellen. */
    private static double round3(double v) {
        return Math.round(v * 1000d) / 1000d;
    }

    /** Publiziert den aktuellen Aggregatzustand an die GUI via RabbitMQ. */
    private void publish(UsageHourly row) {
        String hourIso = row.getHour().atOffset(ZoneOffset.UTC).toString(); // z. B. 2025-01-10T14:00Z
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
