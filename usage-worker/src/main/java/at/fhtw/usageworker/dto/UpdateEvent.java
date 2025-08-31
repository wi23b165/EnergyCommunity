package at.fhtw.usageworker.dto;

public record UpdateEvent(
        String hourIso,            // 2025-01-10T14:00:00Z
        double communityProduced,
        double communityUsed,
        double gridUsed
) {}
