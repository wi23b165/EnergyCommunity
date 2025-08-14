package at.fhtw.restapi.services;

import at.fhtw.restapi.repositories.EnergyReadingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnergyService {
    private final EnergyReadingRepository repo;

    public EnergyService(EnergyReadingRepository repo) {
        this.repo = repo;
    }

    public EnergyUsageDTO getCurrentHour() {
        // Implement as you like; simplest: aggregate current hour window
        LocalDateTime nowHour = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        List<Object[]> rows = repo.aggregateByHour(nowHour, nowHour.plusHours(1));
        return rows.isEmpty() ? null : mapRow(rows.get(0));
    }

    public List<EnergyUsageDTO> aggregateByHour(LocalDateTime from, LocalDateTime to) {
        return repo.aggregateByHour(from, to).stream().map(this::mapRow).toList();
    }

    private EnergyUsageDTO mapRow(Object[] r) {
        // r[0]=hour, r[1]=produced, r[2]=used, r[3]=grid
        String hourIso = toIso(r[0]);
        double produced = toDouble(r[1]);
        double used     = toDouble(r[2]);
        double grid     = toDouble(r[3]);
        return new EnergyUsageDTO(hourIso, produced, used, grid);
    }

    private String toIso(Object v) {
        if (v instanceof Timestamp ts) return ts.toLocalDateTime().toString();
        if (v instanceof LocalDateTime t) return t.toString();
        return String.valueOf(v);
    }

    private double toDouble(Object v) {
        if (v == null) return 0d;
        if (v instanceof Number n) return n.doubleValue();
        if (v instanceof BigDecimal bd) return bd.doubleValue();
        return Double.parseDouble(v.toString());
    }
}
