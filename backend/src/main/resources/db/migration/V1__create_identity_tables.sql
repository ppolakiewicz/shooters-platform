create table user_accounts (
    id uuid primary key,
    email varchar(320) not null,
    password_hash varchar(255) not null,
    enabled boolean not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create unique index uk_user_accounts_email on user_accounts (email);

create table roles (
    name varchar(64) primary key
);

insert into roles (name) values ('USER');

create table user_account_roles (
    user_account_id uuid not null references user_accounts(id) on delete cascade,
    role_name varchar(64) not null references roles(name),
    primary key (user_account_id, role_name)
);
