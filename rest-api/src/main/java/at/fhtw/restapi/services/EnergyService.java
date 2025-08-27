package at.fhtw.restapi.services;

import at.fhtw.restapi.entities.UsageHourly;
import at.fhtw.restapi.repositories.UsageHourlyRepository;
import at.fhtw.restapi.services.dto.DaySummaryDTO;
import at.fhtw.restapi.services.dto.EnergyUsageDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class EnergyService {

    private final UsageHourlyRepository usageRepo;

    public EnergyService(UsageHourlyRepository usageRepo) {
        this.usageRepo = usageRepo;
    }

    // Aktuellste Stunde
    public EnergyUsageDTO getCurrentHour() {
        return usageRepo.findTopByOrderByHourDesc()
                .map(this::map)
                .orElse(null);
    }

    // Bereich (inkl. from, exkl. to+1T wird im Controller erledigt)
    public List<EnergyUsageDTO> getHourly(LocalDateTime from, LocalDateTime to) {
        return usageRepo.findAllByHourBetweenOrderByHourAsc(from, to)
                .stream().map(this::map).toList();
    }

    // Tagessumme (aus hourly)
    public DaySummaryDTO getDaySummary(LocalDate day) {
        Objects.requireNonNull(day, "day");
        LocalDateTime from = day.atStartOfDay();
        LocalDateTime to   = day.plusDays(1).atStartOfDay();

        double produced = 0, used = 0, grid = 0;
        for (UsageHourly h : usageRepo.findAllByHourBetweenOrderByHourAsc(from, to)) {
            produced += h.getCommunityProduced();
            used     += h.getCommunityUsed();
            grid     += h.getGridUsed();
        }
        return new DaySummaryDTO(day, produced, used, grid);
    }

    private EnergyUsageDTO map(UsageHourly u) {
        return new EnergyUsageDTO(u.getHour(), u.getCommunityProduced(), u.getCommunityUsed(), u.getGridUsed());
    }
}
