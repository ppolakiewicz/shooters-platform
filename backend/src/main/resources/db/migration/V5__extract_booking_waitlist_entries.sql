drop index ux_booking_reservations_active_email;
drop index ix_booking_reservations_waitlist_order;

alter table booking_reservations
    drop constraint ck_booking_reservations_waitlist_position,
    drop column waitlist_position;

create unique index ux_booking_reservations_active_email
    on booking_reservations (term_id, email)
    where status in ('CONFIRMED', 'WAITLIST_OFFERED');

create table booking_waitlist_entries
(
    id                  uuid primary key,
    term_id             uuid                     not null references booking_terms (id) on delete cascade,
    participant_user_id uuid                     references user_accounts (id) on delete set null,
    first_name          varchar(80)              not null,
    last_name           varchar(80)              not null,
    email               varchar(320)             not null,
    phone_number        varchar(40)              not null,
    position            integer                  not null,
    cancellation_token  varchar(64)              not null unique,
    created_at          timestamp with time zone not null,
    updated_at          timestamp with time zone not null,
    constraint ck_booking_waitlist_entries_position check (position > 0),
    constraint ux_booking_waitlist_entries_term_position unique (term_id, position)
);

create index ix_booking_waitlist_entries_term_order
    on booking_waitlist_entries (term_id, position, created_at);

create unique index ux_booking_waitlist_entries_email
    on booking_waitlist_entries (term_id, email);
