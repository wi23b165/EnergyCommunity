package at.fhtw.percentage.messaging;

public record UpdateEvent(
        String hourIso,
        Double communityUsed,
        Double gridUsed,
        Double communityProduced
) {}
