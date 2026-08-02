create table if not exists room
(
    id       uuid
        constraint room_pk primary key,
    number   varchar not null,
    capacity int     not null
);