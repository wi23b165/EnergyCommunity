// src/main/java/at/fhtw/consumermeter/persistence/ProductionRecord.java
package at.fhtw.consumermeter.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "production")
public class ProductionRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String producerId;
    private String sourceType;
    private double producedKwh;
    private Instant timestamp;

    // getters/setters
    public Long getId() { return id; }
    public String getProducerId() { return producerId; }
    public void setProducerId(String producerId) { this.producerId = producerId; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public double getProducedKwh() { return producedKwh; }
    public void setProducedKwh(double producedKwh) { this.producedKwh = producedKwh; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
