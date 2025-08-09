package at.fhtw.restapi.services;

import at.fhtw.restapi.dto.EnergyUsageDTO;
import at.fhtw.restapi.repositories.EnergyReadingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EnergyService {
    private final EnergyReadingRepository repo;
    private final DateTimeFormatter iso = DateTimeFormatter.ISO_DATE_TIME;

    public EnergyService(EnergyReadingRepository repo) {
        this.repo = repo;
    }

    public List<EnergyUsageDTO> aggregateByHour(LocalDateTime from, LocalDateTime to) {
        return repo.aggregateByHour(from, to)
                .stream()
                .map(row -> new EnergyUsageDTO(
                        ((java.sql.Timestamp) row[0]).toLocalDateTime().format(iso),
                        ((Number) row[1]).doubleValue(),
                        ((Number) row[2]).doubleValue(),
                        ((Number) row[3]).doubleValue()
                ))
                .toList();
    }
}