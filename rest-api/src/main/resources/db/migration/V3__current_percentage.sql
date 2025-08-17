create table if not exists current_percentage (
                                                  hour timestamp primary key,
                                                  community_depleted numeric(6,2) not null,
    grid_portion       numeric(6,2) not null
    );
