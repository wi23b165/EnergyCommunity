package at.fhtw.restapi.repositories;

import at.fhtw.restapi.dto.CurrentPercentageDto;
import at.fhtw.restapi.dto.UsageHourlyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EnergyReadRepository {

    private final JdbcTemplate jdbc;

    private static OffsetDateTime toOdt(Timestamp ts) {
        return ts.toInstant().atOffset(ZoneOffset.UTC);
    }

    // <--- HIER: hour statt hour_ts
    private static final RowMapper<CurrentPercentageDto> CURRENT_MAPPER = (rs, n) ->
            new CurrentPercentageDto(
                    toOdt(rs.getTimestamp("hour")),
                    rs.getDouble("used_kwh"),
                    rs.getDouble("grid_used_kwh"),
                    rs.getDouble("community_pct")
            );

    private static final RowMapper<UsageHourlyDto> HOURLY_MAPPER = (rs, n) ->
            new UsageHourlyDto(
                    toOdt(rs.getTimestamp("hour")),
                    rs.getDouble("community_produced"),
                    rs.getDouble("community_used"),
                    rs.getDouble("grid_used")
            );

    /** letzte (max) Stunde aus current_percentage. */
    public Optional<CurrentPercentageDto> findCurrent() {
        var sql = """
            SELECT hour, used_kwh, grid_used_kwh, community_pct
            FROM current_percentage
            ORDER BY hour DESC
            LIMIT 1
        """;
        var list = jdbc.query(sql, CURRENT_MAPPER);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /** Historische Nutzung (inkl. grid) im Intervall [start,end]. */
    public List<UsageHourlyDto> findUsageBetween(OffsetDateTime start, OffsetDateTime end) {
        var sql = """
            SELECT hour, community_produced, community_used, grid_used
            FROM usage_hourly
            WHERE hour >= ? AND hour <= ?
            ORDER BY hour ASC
        """;
        return jdbc.query(sql, HOURLY_MAPPER,
                Timestamp.from(start.toInstant()),
                Timestamp.from(end.toInstant()));
    }
}
