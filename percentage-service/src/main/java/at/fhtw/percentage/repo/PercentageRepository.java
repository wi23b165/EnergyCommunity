package at.fhtw.percentage.repo;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class PercentageRepository {

    private final JdbcTemplate jdbc;

    @PostConstruct
    public void ensureTable() {
        // 1) Tabelle anlegen, wenn sie fehlt
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS current_percentage (
                hour            TIMESTAMPTZ PRIMARY KEY,
                used_kwh        DOUBLE PRECISION NOT NULL,
                grid_used_kwh   DOUBLE PRECISION NOT NULL,
                community_pct   DOUBLE PRECISION NOT NULL
            )
        """);

        // 2) updated_at sicherstellen (falls alte Tabelle ohne diese Spalte existiert)
        jdbc.execute("""
            ALTER TABLE current_percentage
            ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT now() NOT NULL
        """);

        // 3) Indizes
        jdbc.execute("""
            CREATE INDEX IF NOT EXISTS idx_current_percentage_hour
            ON current_percentage(hour DESC)
        """);

        jdbc.execute("""
            CREATE INDEX IF NOT EXISTS idx_current_percentage_updated
            ON current_percentage(updated_at DESC)
        """);
    }

    @Transactional
    public void upsert(OffsetDateTime hour, double used, double grid, double pct) {
        // WICHTIG: hour (nicht hour_ts)
        final String sql = """
            INSERT INTO current_percentage (hour, used_kwh, grid_used_kwh, community_pct)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (hour) DO UPDATE
               SET used_kwh      = EXCLUDED.used_kwh,
                   grid_used_kwh = EXCLUDED.grid_used_kwh,
                   community_pct = EXCLUDED.community_pct,
                   updated_at    = now()
        """;
        jdbc.update(sql,
                Timestamp.from(hour.toInstant()),
                used, grid, pct);
    }
}
