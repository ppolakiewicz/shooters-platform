alter table user_accounts
    add column username varchar(32) not null ;

create unique index uk_user_accounts_username_ci on user_accounts (lower(username));
