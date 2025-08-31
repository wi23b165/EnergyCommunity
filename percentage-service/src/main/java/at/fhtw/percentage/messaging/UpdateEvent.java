package at.fhtw.percentage.messaging;

public record UpdateEvent(
        String hourIso,          // z.B. "2025-08-27T15:00" (optional – falls null, wird "jetzige Stunde" verwendet)
        Double communityUsed,    // kWh in dieser Stunde
        Double gridUsed,         // kWh in dieser Stunde
        Double communityProduced // optional: nicht zwingend für die Prozent-Berechnung
) {}
