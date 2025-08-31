package at.fhtw.usageworker.model;

import lombok.Data;
import java.time.Instant;

@Data
public class ProductionEvent {
    private String producerId;     // optional, kommt vom Simulator
    private String sourceType;     // optional (SOLAR/WIND/…)
    private double producedKwh;    // wichtig
    private Instant timestamp;     // wichtig
}
