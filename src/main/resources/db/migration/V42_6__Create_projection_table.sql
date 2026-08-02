create table if not exists projection
(
    id         uuid
        constraint projection_pk primary key,
    datetime   timestamptz    not null,
    seat_price numeric(10, 2) not null,
    movie_id   uuid           not null
        constraint projection_movie_fk references movie (id),
    room_id    uuid           not null
        constraint projection_room_fk references room (id)
);

create index if not exists projection_movie_id_idx on projection (movie_id);
create index if not exists projection_room_id_idx on projection (room_id);