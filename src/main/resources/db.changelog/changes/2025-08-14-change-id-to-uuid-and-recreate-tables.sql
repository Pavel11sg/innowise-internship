--liquibase formatted sql

--changeset pavel11sg:recreate_users_table_with_uuid
drop table if exists card_info cascade;
drop table if exists users cascade;

create table users
(
    id         uuid primary key default gen_random_uuid(),
    name       varchar(100)        not null,
    surname    varchar(100)        not null,
    birth_date date           not null check (birth_date > '1900-01-01' and birth_date < current_date),
    email      varchar(255) unique not null check (email like '%@%.%')
);

--changeset pavel11sg:recreate_card_info_table_with_uuid
create table card_info
(
    id              uuid primary key default gen_random_uuid(),
    user_id         uuid      not null,
    number          varchar(19) not null check (number ~ '^[0-9]{16,19}$'),
    holder          varchar     not null,
    expiration_date date        not null,
    constraint fk_user_id foreign key (user_id) references users (id) on delete cascade
);

--changeset pavel11sg:recreate_indexes
create index idx_users_name_surname on users (name, surname);
create index idx_users_email on users (email);
create index idx_card_info_user_id on card_info (user_id);
create index idx_card_info_number on card_info (number);