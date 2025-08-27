package at.fhtw.restapi.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "energy_reading")
public class EnergyReading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "community_produced", nullable = false)
    private double communityProduced;

    @Column(name = "community_used", nullable = false)
    private double communityUsed;

    @Column(name = "grid_used", nullable = false)
    private double gridUsed;

    public EnergyReading() {}

    public EnergyReading(LocalDateTime recordedAt, double communityProduced, double communityUsed, double gridUsed) {
        this.recordedAt = recordedAt;
        this.communityProduced = communityProduced;
        this.communityUsed = communityUsed;
        this.gridUsed = gridUsed;
    }

    public Long getId() { return id; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public double getCommunityProduced() { return communityProduced; }
    public double getCommunityUsed() { return communityUsed; }
    public double getGridUsed() { return gridUsed; }

    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public void setCommunityProduced(double communityProduced) { this.communityProduced = communityProduced; }
    public void setCommunityUsed(double communityUsed) { this.communityUsed = communityUsed; }
    public void setGridUsed(double gridUsed) { this.gridUsed = gridUsed; }
}
