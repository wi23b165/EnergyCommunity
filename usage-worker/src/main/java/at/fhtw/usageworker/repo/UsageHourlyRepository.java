package at.fhtw.usageworker.repo;

import at.fhtw.usageworker.model.UsageHourly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository für stündliche Aggregationen.
 * PK: hour (UTC, LocalDateTime ohne TZ).
 */
public interface UsageHourlyRepository extends JpaRepository<UsageHourly, LocalDateTime> {

    // --- Für deinen UsageQueryController ---

    /** Bereichsabfrage mit aufsteigender Sortierung nach hour. */
    List<UsageHourly> findByHourBetweenOrderByHourAsc(LocalDateTime from, LocalDateTime to);

    /** Paginiert die neuesten Einträge (absteigend nach hour). */
    Page<UsageHourly> findAllByOrderByHourDesc(Pageable pageable);

    // --- Konkurrenzsicheres UPSERT (Option 1) ---

    /**
     * Addiert Deltas atomisch und setzt grid_used = max(used - produced, 0).
     * Robust gegen parallele Listener.
     */
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO usage_hourly (hour, community_produced, community_used, grid_used)
        VALUES (:hour, :producedDelta, :usedDelta, GREATEST(:usedDelta - :producedDelta, 0))
        ON CONFLICT (hour) DO UPDATE
        SET community_produced = usage_hourly.community_produced + EXCLUDED.community_produced,
            community_used     = usage_hourly.community_used     + EXCLUDED.community_used,
            grid_used          = GREATEST(
                                   (usage_hourly.community_used + EXCLUDED.community_used)
                                   - (usage_hourly.community_produced + EXCLUDED.community_produced),
                                   0
                                 )
        """, nativeQuery = true)
    void upsertDeltaRecomputeGrid(@Param("hour") LocalDateTime hour,
                                  @Param("producedDelta") double producedDelta,
                                  @Param("usedDelta") double usedDelta);
}
