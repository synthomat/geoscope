create table access_tokens
(
    id serial
        constraint access_tokens_pk
            primary key,
    token varchar(40) not null,
    created_at timestamptz default now() not null,
    valid_until timestamptz
);
--;;
create unique index access_tokens_token_uindex
    on access_tokens (token);
