create table if not exists movie
(
    id          uuid
        constraint movie_pk primary key,
    title       varchar not null,
    genre       varchar not null
        constraint movie_genre_check
            check (genre in
                   ('THRILLER', 'ROMANCE', 'COMEDY', 'DRAMA', 'ACTION', 'SCI_FI', 'FANTASY', 'ANIMATION')),
    description varchar,
    duration    numeric not null
);