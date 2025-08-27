package at.fhtw.restapi.services.dto;

import java.time.LocalDate;

public class DaySummaryDTO {
    private LocalDate day;
    private double produced;
    private double used;
    private double grid;
    private double autarkyPercent;   // produced / used * 100
    private double gridSharePercent; // grid / used * 100

    public DaySummaryDTO() {}

    public DaySummaryDTO(LocalDate day, double produced, double used, double grid) {
        this.day = day;
        this.produced = produced;
        this.used = used;
        this.grid = grid;
        this.autarkyPercent = used > 0 ? (produced / used) * 100.0 : 0.0;
        this.gridSharePercent = used > 0 ? (grid / used) * 100.0 : 0.0;
    }

    public LocalDate getDay() { return day; }
    public double getProduced() { return produced; }
    public double getUsed() { return used; }
    public double getGrid() { return grid; }
    public double getAutarkyPercent() { return autarkyPercent; }
    public double getGridSharePercent() { return gridSharePercent; }

    public void setDay(LocalDate day) { this.day = day; }
    public void setProduced(double produced) { this.produced = produced; }
    public void setUsed(double used) { this.used = used; }
    public void setGrid(double grid) { this.grid = grid; }
    public void setAutarkyPercent(double v) { this.autarkyPercent = v; }
    public void setGridSharePercent(double v) { this.gridSharePercent = v; }
}
