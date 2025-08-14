package at.fhtw.restapi.repositories;

import at.fhtw.restapi.entities.EnergyReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EnergyReadingRepository extends JpaRepository<EnergyReading, Long> {

    // hour, produced, used, grid   (PostgreSQL)
    @Query(value = """
        SELECT date_trunc('hour', recorded_at) AS hour,
               SUM(community_produced)        AS produced,
               SUM(community_used)            AS used,
               SUM(grid_used)                 AS grid
        FROM energy_reading
        WHERE recorded_at >= :from AND recorded_at < :to
        GROUP BY hour
        ORDER BY hour DESC
        """, nativeQuery = true)
    List<Object[]> aggregateByHour(@Param("from") LocalDateTime from,
                                   @Param("to")   LocalDateTime to);
}
