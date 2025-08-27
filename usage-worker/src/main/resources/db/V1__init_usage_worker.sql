CREATE TABLE IF NOT EXISTS energy_reading (
                                              id BIGSERIAL PRIMARY KEY,
                                              ts TIMESTAMPTZ NOT NULL,
                                              community_used DOUBLE PRECISION NOT NULL,
                                              grid_used DOUBLE PRECISION NOT NULL,
                                              community_produced DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS usage_hourly (
                                            hour TIMESTAMP WITHOUT TIME ZONE PRIMARY KEY,
                                            community_produced DOUBLE PRECISION NOT NULL DEFAULT 0,
                                            community_used DOUBLE PRECISION NOT NULL DEFAULT 0,
                                            grid_used DOUBLE PRECISION NOT NULL DEFAULT 0
);
