// src/main/java/at/fhtw/producer/model/ProductionEvent.java
package at.fhtw.producer.model;

import java.time.Instant;

public record ProductionEvent(
        String producerId,       // z.B. "solar-1"
        String sourceType,       // z.B. "SOLAR"
        double producedKwh,      // erzeugte kWh
        Instant timestamp
) {}
