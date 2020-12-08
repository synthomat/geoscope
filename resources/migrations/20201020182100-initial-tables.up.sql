CREATE TABLE IF NOT EXISTS geopoints
(
    id        serial
        constraint geodata_pk
            primary key,
    geom      geometry    not null,
    props     json,
    timestamp timestamptz not null
);