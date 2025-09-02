package at.fhtw.percentage.service;

import at.fhtw.percentage.messaging.UpdateEvent;
import at.fhtw.percentage.repo.PercentageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PercentageListener {

    private final PercentageRepository repo;

    /** Wird vom UpdateListener (Rabbit) aufgerufen oder kann im Test direkt benutzt werden. */
    public void apply(UpdateEvent evt) {
        try {
            // 1) Stunde parsen/auf volle Stunde UTC kürzen
            OffsetDateTime hour = parseHour(evt.hourIso());

            // 2) Zahlen sichern
            double used = nz(evt.communityUsed());
            double grid = nz(evt.gridUsed());

            // 3) Prozent berechnen: 100 * (1 - grid/used)
            double pct = computeCommunityPct(used, grid);

            // 4) Upsert (OffsetDateTime wird in der DB als timestamptz gespeichert)
            repo.upsert(hour, used, grid, pct);

            log.info("✅ updated percentage_current hour={} used={} grid={} pct={}",
                    hour, round2(used), round2(grid), round2(pct));
        }
        catch (Exception ex) {
            log.error("Failed to process UpdateEvent: {}", evt, ex);
        }
    }

    private static OffsetDateTime parseHour(String hourIso) {
        OffsetDateTime odt = (hourIso == null || hourIso.isBlank())
                ? OffsetDateTime.now(ZoneOffset.UTC)
                : OffsetDateTime.parse(hourIso);
        return odt.truncatedTo(ChronoUnit.HOURS);
    }

    private static double nz(Double d) { return d == null ? 0.0 : d; }

    private static double computeCommunityPct(double used, double grid) {
        if (used <= 0.0) return 0.0;
        double pct = (1.0 - (grid / used)) * 100.0;
        pct = Math.max(0.0, Math.min(100.0, pct));
        return Math.round(pct * 100.0) / 100.0;
    }

    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
