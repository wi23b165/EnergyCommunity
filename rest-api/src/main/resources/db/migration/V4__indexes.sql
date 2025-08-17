-- beschleunigt Zeitbereichs-Abfragen (z. B. letzte Stunde/Tag)
CREATE INDEX IF NOT EXISTS idx_energy_reading_recorded_at
    ON energy_reading (recorded_at);

-- falls du oft nach "Stundenaggregaten" fragst, hilft ein Ausdrucksindex:
-- (nutzt dieselbe Funktion, die du in der hourly-Aggregation verwendest)
CREATE INDEX IF NOT EXISTS idx_energy_reading_recorded_hour
    ON energy_reading ((date_trunc('hour', recorded_at)));

-- OPTIONAL: wenn du häufig nach Kombinationen filterst (z. B. Zeitraum + Mindestproduktion),
-- kannst du weitere Indizes ergänzen. Erst messen, dann hinzufügen.
-- CREATE INDEX IF NOT EXISTS idx_energy_reading_recorded_at_produced
--   ON energy_reading (recorded_at, community_produced);
