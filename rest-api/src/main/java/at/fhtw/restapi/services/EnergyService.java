package at.fhtw.restapi.services;

import at.fhtw.restapi.entities.UsageHourly;
import at.fhtw.restapi.repositories.UsageHourlyRepository;
import at.fhtw.restapi.services.dto.CurrentPercentageDTO;   // <-- neu
import at.fhtw.restapi.services.dto.DaySummaryDTO;
import at.fhtw.restapi.services.dto.EnergyUsageDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class EnergyService {

    private final UsageHourlyRepository usageRepo;

    public EnergyService(UsageHourlyRepository usageRepo) {
        this.usageRepo = usageRepo;
    }

    /** Aktuellste verfügbare Stunde (Usage-Rohwerte). */
    public EnergyUsageDTO getCurrentHour() {
        return usageRepo.findTopByOrderByHourDesc()
                .map(this::map)
                .orElse(null);
    }

    /** NEU: Prozentwerte (community_depleted & grid_portion) für die aktuellste Stunde. */
    public CurrentPercentageDTO getCurrentPercentage() {
        return usageRepo.findTopByOrderByHourDesc()
                .map(u -> {
                    double produced = nz(u.getCommunityProduced());
                    double used     = nz(u.getCommunityUsed());
                    double grid     = nz(u.getGridUsed());

                    // grid_portion = Anteil des Netzes am Gesamtverbrauch
                    double gridPortion = (used <= 0) ? 0.0 : (grid / used) * 100.0;

                    // community_depleted = wie stark ist der Community-Pool für diese Stunde beansprucht
                    double communityDepleted;
                    if (produced <= 0) {
                        communityDepleted = (used > 0) ? 100.0 : 0.0;
                    } else {
                        communityDepleted = (used >= produced) ? 100.0 : (used / produced) * 100.0;
                    }

                    // auf 2 Nachkommastellen runden
                    gridPortion = r2(gridPortion);
                    communityDepleted = r2(communityDepleted);

                    String hourIso = u.getHour().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    return new CurrentPercentageDTO(hourIso, communityDepleted, gridPortion);
                })
                .orElse(null);
    }

    /** Stundenwerte im Bereich [from, to) – to ist exklusiv. */
    public List<EnergyUsageDTO> getHourly(LocalDateTime from, LocalDateTime to) {
        return usageRepo.findAllByHourBetweenOrderByHourAsc(from, to)
                .stream()
                .map(this::map)
                .toList();
    }

    /** Tagessumme aus den Stundenwerten. */
    public DaySummaryDTO getDaySummary(LocalDate day) {
        Objects.requireNonNull(day, "day");
        LocalDateTime from = day.atStartOfDay();
        LocalDateTime to   = day.plusDays(1).atStartOfDay();

        double produced = 0, used = 0, grid = 0;
        for (UsageHourly h : usageRepo.findAllByHourBetweenOrderByHourAsc(from, to)) {
            produced += nz(h.getCommunityProduced());
            used     += nz(h.getCommunityUsed());
            grid     += nz(h.getGridUsed());
        }
        return new DaySummaryDTO(day, produced, used, grid);
    }

    /** Entity -> DTO Mapping (Null-sicher). */
    private EnergyUsageDTO map(UsageHourly u) {
        return new EnergyUsageDTO(
                u.getHour(),
                nz(u.getCommunityProduced()),
                nz(u.getCommunityUsed()),
                nz(u.getGridUsed())
        );
    }

    private static double nz(Double d) { return d == null ? 0.0 : d; }
    private static double r2(double v) { return Math.round(v * 100.0) / 100.0; }
}
