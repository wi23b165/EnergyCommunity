package at.fhtw.consumer.dto;

import java.time.OffsetDateTime;

public record EnergyMessage(
        String type,          // "USAGE"
        double kwh,           // Energie im Intervall
        String source,        // z.B. "user-1"
        OffsetDateTime recordedAt
) {}
