create table if not exists reservation_seat
(
    reservation_id uuid not null
        constraint reservation_seat_reservation_fk references reservation (id),
    seat_id        uuid not null
        constraint reservation_seat_seat_fk references seat (id),
    constraint reservation_seat_pk primary key (reservation_id, seat_id)
);

create index if not exists reservation_seat_seat_id_idx on reservation_seat (seat_id);