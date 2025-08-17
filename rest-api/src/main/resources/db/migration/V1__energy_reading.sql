create table if not exists energy_reading (
                                              id bigserial primary key,
                                              recorded_at timestamp not null,
                                              community_produced numeric(12,3) not null,
    community_used     numeric(12,3) not null,
    grid_used          numeric(12,3) not null
    );
create index if not exists idx_er_recorded_at on energy_reading(recorded_at);
