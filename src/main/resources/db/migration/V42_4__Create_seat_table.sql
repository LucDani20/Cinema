create table if not exists seat
(
    id      uuid
        constraint seat_pk primary key,
    number  varchar not null,
    room_id uuid    not null
        constraint seat_room_fk references room (id)
);

create index if not exists seat_room_id_idx on seat (room_id);