package at.fhtw.usage;

import java.time.Instant;

public record ProducedEvent(Instant timestamp, double kwh) { }
