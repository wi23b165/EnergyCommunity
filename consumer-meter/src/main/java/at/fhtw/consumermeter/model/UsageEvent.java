// src/main/java/at/fhtw/consumermeter/model/UsageEvent.java
package at.fhtw.consumermeter.model;

import java.time.Instant;

public record UsageEvent(
        String communityId,
        double communityUsed,
        double gridUsed,
        Instant timestamp
) {}
