// src/main/java/at/fhtw/producer/model/UsageEvent.java
package at.fhtw.producer.model;

import java.time.Instant;

public record UsageEvent(
        String communityId,      // z.B. "ec-01"
        double communityUsed,    // Verbrauch der Community (kWh)
        double gridUsed,         // zusätzlich aus Netz bezogen (kWh)
        Instant timestamp
) {}
