create table if not exists usage_hourly (
                                            hour timestamp primary key,
                                            community_produced numeric(12,3) not null,
    community_used     numeric(12,3) not null,
    grid_used          numeric(12,3) not null
    );
create index if not exists idx_uh_hour on usage_hourly(hour);
