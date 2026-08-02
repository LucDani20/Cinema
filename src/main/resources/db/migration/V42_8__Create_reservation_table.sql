create table if not exists reservation
(
    id            uuid
        constraint reservation_pk primary key,
    created_at    timestamptz not null,
    projection_id uuid        not null
        constraint reservation_projection_fk references projection (id),
    user_id       uuid        not null
        constraint reservation_user_fk references app_user (id)
);

create index if not exists reservation_projection_id_idx on reservation (projection_id);
create index if not exists reservation_user_id_idx on reservation (user_id);