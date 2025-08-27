// src/main/java/at/fhtw/consumermeter/model/ProductionEvent.java
package at.fhtw.consumermeter.model;

import java.time.Instant;

public record ProductionEvent(
        String producerId,
        String sourceType,
        double producedKwh,
        Instant timestamp
) {}
