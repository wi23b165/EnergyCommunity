package at.fhtw.restapi.repositories;

import at.fhtw.restapi.entities.EnergyReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EnergyReadingRepository extends JpaRepository<EnergyReading, Long> {

    @Query(value = """
        SELECT date_trunc('hour', recorded_at) AS hour,
               SUM(community_produced)        AS produced,
               SUM(community_used)            AS used,
               SUM(grid_used)                 AS grid
        FROM energy_reading
        WHERE recorded_at >= :from AND recorded_at < :to
        GROUP BY hour
        ORDER BY hour ASC
        """, nativeQuery = true)
    List<Object[]> aggregateByHour(@Param("from") LocalDateTime from,
                                   @Param("to")   LocalDateTime to);

    @Query(value = """
        SELECT COALESCE(SUM(community_produced),0),
               COALESCE(SUM(community_used),0),
               COALESCE(SUM(grid_used),0)
        FROM energy_reading
        WHERE recorded_at >= :start AND recorded_at < :end
        """, nativeQuery = true)
    Object[] dayTotals(@Param("start") LocalDateTime start,
                       @Param("end")   LocalDateTime end);

    List<EnergyReading> findAllByOrderByRecordedAtDesc();

    default Object[] dayTotals(LocalDate day) {
        return dayTotals(day.atStartOfDay(), day.plusDays(1).atStartOfDay());
    }
}
