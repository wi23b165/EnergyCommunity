// src/main/java/at/fhtw/producer/model/TickResult.java
package at.fhtw.producer.model;

public record TickResult(ProductionEvent produced, UsageEvent used) {}
