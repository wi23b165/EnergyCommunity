package at.fhtw.restapi.dto;

import java.time.OffsetDateTime;

public record CurrentPercentageDto(
        OffsetDateTime hour,
        double usedKwh,
        double gridUsedKwh,
        double communityPct
) {}
