package at.fhtw.usageworker.service;

import at.fhtw.usageworker.model.UsageEvent;
import at.fhtw.usageworker.model.UsageHourly;
import at.fhtw.usageworker.repo.UsageHourlyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsageService {

    private final UsageHourlyRepository usageRepo;

    /** Wird vom Rabbit-Listener aufgerufen – aggregiert pro Stunde. */
    @Transactional
    public void apply(UsageEvent evt) {
        LocalDateTime hour = truncateToHour(evt.getTimestamp());
        UsageHourly row = usageRepo.findById(hour)
                .orElseGet(() -> new UsageHourly(hour, 0d, 0d, 0d));

        row.setCommunityUsed(round3(row.getCommunityUsed() + evt.getCommunityUsed()));
        row.setGridUsed(round3(row.getGridUsed() + evt.getGridUsed()));
        row.setCommunityProduced(round3(row.getCommunityProduced() + evt.getCommunityProduced()));

        usageRepo.save(row);
        log.info("✅ aggregated hour={}, produced={}, used={}, grid={}",
                hour, row.getCommunityProduced(), row.getCommunityUsed(), row.getGridUsed());
    }

    private static LocalDateTime truncateToHour(Instant ts) {
        return LocalDateTime.ofInstant(ts, ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.HOURS);
    }

    private static double round3(double v) {
        return Math.round(v * 1000d) / 1000d;
    }
}
