// src/main/java/at/fhtw/consumermeter/persistence/ConsumptionRecord.java
package at.fhtw.consumermeter.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "consumption")
public class ConsumptionRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String communityId;
    private double communityUsed;
    private double gridUsed;
    private Instant timestamp;

    // getters/setters
    public Long getId() { return id; }
    public String getCommunityId() { return communityId; }
    public void setCommunityId(String communityId) { this.communityId = communityId; }
    public double getCommunityUsed() { return communityUsed; }
    public void setCommunityUsed(double communityUsed) { this.communityUsed = communityUsed; }
    public double getGridUsed() { return gridUsed; }
    public void setGridUsed(double gridUsed) { this.gridUsed = gridUsed; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
