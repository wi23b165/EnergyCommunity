package at.fhtw.usageworker.repo;

import at.fhtw.usageworker.model.EnergyReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnergyReadingRepository extends JpaRepository<EnergyReading, Long> {}
