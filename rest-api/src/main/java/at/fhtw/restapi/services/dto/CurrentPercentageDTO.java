package at.fhtw.restapi.services.dto;

public class CurrentPercentageDTO {
    private String hour;           // ISO-String (z.B. 2025-08-30T20:00:00)
    private double communityDepleted; // %
    private double gridPortion;       // %

    public CurrentPercentageDTO() { }
    public CurrentPercentageDTO(String hour, double communityDepleted, double gridPortion) {
        this.hour = hour;
        this.communityDepleted = communityDepleted;
        this.gridPortion = gridPortion;
    }

    public String getHour() { return hour; }
    public double getCommunityDepleted() { return communityDepleted; }
    public double getGridPortion() { return gridPortion; }

    public void setHour(String hour) { this.hour = hour; }
    public void setCommunityDepleted(double v) { this.communityDepleted = v; }
    public void setGridPortion(double v) { this.gridPortion = v; }
}
