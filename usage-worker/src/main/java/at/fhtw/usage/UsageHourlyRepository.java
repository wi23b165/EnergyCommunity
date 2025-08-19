package at.fhtw.usage;

import at.fhtw.usage.domain.UsageHourly;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface UsageHourlyRepository extends JpaRepository<UsageHourly, LocalDateTime> {}
