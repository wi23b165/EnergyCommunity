package at.fhtw.restapi.services.dto;


public class UsageSummaryDTO {
    private double produced;
    private double used;
    private double grid;

    public UsageSummaryDTO(double produced, double used, double grid) {
        this.produced = produced;
        this.used = used;
        this.grid = grid;
    }

    public double getProduced() { return produced; }
    public double getUsed() { return used; }
    public double getGrid() { return grid; }

    public void setProduced(double produced) { this.produced = produced; }
    public void setUsed(double used) { this.used = used; }
    public void setGrid(double grid) { this.grid = grid; }
}
