create table if not exists app_user
(
    id         uuid
        constraint app_user_pk primary key,
    first_name varchar not null,
    last_name  varchar not null,
    birthdate  date    not null,
    email      varchar not null
        constraint app_user_email_uk unique,
    password   varchar not null,
    phone      varchar,
    role       varchar not null
        constraint app_user_role_check check (role in ('CLIENT', 'EMPLOYEE', 'MANAGER'))
);