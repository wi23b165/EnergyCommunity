package at.fhtw.usage.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage_hourly")
public class UsageHourly {

    @Id
    @Column(name = "hour", nullable = false)
    private LocalDateTime hour;

    @Column(name = "community_produced", nullable = false)
    private double communityProduced;

    @Column(name = "community_used", nullable = false)
    private double communityUsed;

    @Column(name = "grid_used", nullable = false)
    private double gridUsed;

    protected UsageHourly() { }

    public UsageHourly(LocalDateTime hour, double communityProduced, double communityUsed, double gridUsed) {
        this.hour = hour;
        this.communityProduced = communityProduced;
        this.communityUsed = communityUsed;
        this.gridUsed = gridUsed;
    }

    // getters/setters …
    public LocalDateTime getHour() { return hour; }
    public void setHour(LocalDateTime hour) { this.hour = hour; }
    public double getCommunityProduced() { return communityProduced; }
    public void setCommunityProduced(double v) { this.communityProduced = v; }
    public double getCommunityUsed() { return communityUsed; }
    public void setCommunityUsed(double v) { this.communityUsed = v; }
    public double getGridUsed() { return gridUsed; }
    public void setGridUsed(double v) { this.gridUsed = v; }
}
