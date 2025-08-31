package at.fhtw.percentage.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public class PercentageRepository {

    private final JdbcTemplate jdbc;

    public PercentageRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        ensureTable();
    }

    /** Legt die Tabelle an, falls sie nicht existiert. */
    private void ensureTable() {
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS current_percentage (
              hour_ts        timestamptz PRIMARY KEY,
              used_kwh       numeric      NOT NULL,
              grid_used_kwh  numeric      NOT NULL,
              pct            numeric      NOT NULL,
              updated_at     timestamptz  NOT NULL DEFAULT now()
            );
        """);
    }

    public void upsert(OffsetDateTime hour, double usedKwh, double gridUsedKwh, double pct) {
        jdbc.update("""
            INSERT INTO current_percentage (hour_ts, used_kwh, grid_used_kwh, pct, updated_at)
            VALUES (?, ?, ?, ?, now())
            ON CONFLICT (hour_ts) DO UPDATE
              SET used_kwh = EXCLUDED.used_kwh,
                  grid_used_kwh = EXCLUDED.grid_used_kwh,
                  pct = EXCLUDED.pct,
                  updated_at = now();
        """, hour, usedKwh, gridUsedKwh, pct);
    }
}
