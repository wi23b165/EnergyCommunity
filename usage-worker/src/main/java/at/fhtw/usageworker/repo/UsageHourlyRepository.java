package at.fhtw.usageworker.repo;

import at.fhtw.usageworker.model.UsageHourly;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UsageHourlyRepository extends JpaRepository<UsageHourly, LocalDateTime> {

    // Range (zeitlich aufsteigend für Charts)
    List<UsageHourly> findByHourBetweenOrderByHourAsc(LocalDateTime from, LocalDateTime to);

    // Für „latest“ nutzen wir Pageable, damit limit flexibel ist
    List<UsageHourly> findAllByOrderByHourDesc(Pageable pageable);
}
