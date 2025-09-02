package at.fhtw.restapi.dto;

import java.time.OffsetDateTime;

public record UsageHourlyDto(
        OffsetDateTime hour,
        double communityProduced,
        double communityUsed,
        double gridUsed
) {}
