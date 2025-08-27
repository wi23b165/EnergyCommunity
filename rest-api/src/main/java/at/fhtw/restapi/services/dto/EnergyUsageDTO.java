package at.fhtw.restapi.services.dto;

import java.time.LocalDateTime;

public class EnergyUsageDTO {
    private LocalDateTime hour;
    private double communityProduced;
    private double communityUsed;
    private double gridUsed;

    public EnergyUsageDTO() {}
    public EnergyUsageDTO(LocalDateTime hour, double cp, double cu, double grid) {
        this.hour = hour;
        this.communityProduced = cp;
        this.communityUsed = cu;
        this.gridUsed = grid;
    }

    public LocalDateTime getHour() { return hour; }
    public double getCommunityProduced() { return communityProduced; }
    public double getCommunityUsed() { return communityUsed; }
    public double getGridUsed() { return gridUsed; }

    public void setHour(LocalDateTime hour) { this.hour = hour; }
    public void setCommunityProduced(double v) { this.communityProduced = v; }
    public void setCommunityUsed(double v) { this.communityUsed = v; }
    public void setGridUsed(double v) { this.gridUsed = v; }
}
