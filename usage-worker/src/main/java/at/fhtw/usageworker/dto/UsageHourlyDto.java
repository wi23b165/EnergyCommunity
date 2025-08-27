package at.fhtw.usageworker.dto;

import at.fhtw.usageworker.model.UsageHourly;
import java.time.LocalDateTime;

public record UsageHourlyDto(
        LocalDateTime hour,
        double communityProduced,
        double communityUsed,
        double gridUsed
) {
    public static UsageHourlyDto from(UsageHourly u) {
        return new UsageHourlyDto(u.getHour(), u.getCommunityProduced(), u.getCommunityUsed(), u.getGridUsed());
    }
}
