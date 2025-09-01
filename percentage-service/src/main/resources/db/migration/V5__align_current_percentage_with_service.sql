DO $$
BEGIN
  -- Rename hour -> hour_ts (nur wenn hour existiert)
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema='public' AND table_name='current_percentage' AND column_name='hour'
  ) THEN
    EXECUTE 'ALTER TABLE public.current_percentage RENAME COLUMN hour TO hour_ts';
END IF;

  -- hour_ts auf timestamptz (idempotent)
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema='public' AND table_name='current_percentage' AND column_name='hour_ts'
  ) THEN
    EXECUTE 'ALTER TABLE public.current_percentage
               ALTER COLUMN hour_ts TYPE timestamptz
               USING hour_ts AT TIME ZONE ''UTC''';
END IF;
END $$;

ALTER TABLE public.current_percentage
    ADD COLUMN IF NOT EXISTS used_kwh       NUMERIC(10,3) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS grid_used_kwh  NUMERIC(10,3) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS pct            NUMERIC(6,2)  NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS updated_at     timestamptz   NOT NULL DEFAULT now();

-- WICHTIG: alte Spalten mit Default versehen (sonst krachen Inserts)
ALTER TABLE public.current_percentage
    ALTER COLUMN community_depleted SET DEFAULT 0,
ALTER COLUMN grid_portion       SET DEFAULT 0;

-- PK sicherstellen (auf hour_ts)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'current_percentage_pkey'
  ) THEN
    EXECUTE 'ALTER TABLE public.current_percentage ADD PRIMARY KEY (hour_ts)';
END IF;
END $$;
