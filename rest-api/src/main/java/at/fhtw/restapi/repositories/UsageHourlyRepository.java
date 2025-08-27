package at.fhtw.restapi.repositories;

import at.fhtw.restapi.entities.UsageHourly;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsageHourlyRepository extends JpaRepository<UsageHourly, LocalDateTime> {

    // Bereichsabfrage (für Charts)
    List<UsageHourly> findAllByHourBetweenOrderByHourAsc(LocalDateTime from, LocalDateTime to);

    // NEU: neuester Datensatz (für /energy/current)
    Optional<UsageHourly> findTopByOrderByHourDesc();
}
