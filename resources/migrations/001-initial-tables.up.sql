create table geodata
(
    id serial
        constraint geodata_pk
            primary key,
    point geography(POINT) not null,
    timestamp timestamptz not null
);