package at.fhtw.restapi.services;

import at.fhtw.restapi.dto.CurrentPercentageDto;
import at.fhtw.restapi.dto.UsageHourlyDto;
import at.fhtw.restapi.repositories.EnergyReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnergyService {

    private final EnergyReadRepository repo;

    public Optional<CurrentPercentageDto> getCurrent() {
        return repo.findCurrent();
    }

    public List<UsageHourlyDto> getHistorical(OffsetDateTime start, OffsetDateTime end) {
        if (start == null || end == null) throw new IllegalArgumentException("start and end are required");
        if (end.isBefore(start)) throw new IllegalArgumentException("end must be >= start");
        return repo.findUsageBetween(start, end);
    }
}
