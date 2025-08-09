package at.fhtw.restapi.dto;

public record EnergyUsageDTO(
        String hourIso,
        double communityProduced,
        double communityUsed,
        double gridUsed
) {}