create table password_reset_tokens
(
    id              uuid primary key,
    user_account_id uuid                     not null references user_accounts (id) on delete cascade,
    token_hash      varchar(64)              not null unique,
    expires_at      timestamp with time zone not null,
    used_at         timestamp with time zone,
    created_at      timestamp with time zone not null
);

create index ix_password_reset_tokens_user_account_id on password_reset_tokens (user_account_id);
