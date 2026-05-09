create table booking_training_enrollments (
    id uuid primary key,
    owner_user_id uuid not null references user_accounts(id) on delete cascade,
    name varchar(120) not null,
    description varchar(2048) not null,
    place_name varchar(240) not null,
    address varchar(240) not null,
    latitude double precision not null,
    longitude double precision not null,
    capacity integer not null,
    cancellation_deadline_days integer not null,
    duration_minutes integer not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint ck_booking_training_enrollments_capacity check (capacity > 0),
    constraint ck_booking_training_enrollments_cancellation_days check (cancellation_deadline_days >= 0),
    constraint ck_booking_training_enrollments_duration check (duration_minutes > 0),
    constraint ck_booking_training_enrollments_latitude check (latitude between -90 and 90),
    constraint ck_booking_training_enrollments_longitude check (longitude between -180 and 180)
);

create index ix_booking_training_enrollments_owner on booking_training_enrollments (owner_user_id, created_at desc);

create table booking_terms (
    id uuid primary key,
    owner_user_id uuid not null references user_accounts(id) on delete cascade,
    name varchar(120) not null,
    description varchar(2048) not null,
    place_name varchar(240) not null,
    address varchar(240) not null,
    latitude double precision not null,
    longitude double precision not null,
    capacity integer not null,
    cancellation_deadline_days integer not null,
    duration_minutes integer not null,
    starts_at timestamp not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint ck_booking_terms_capacity check (capacity > 0),
    constraint ck_booking_terms_cancellation_days check (cancellation_deadline_days >= 0),
    constraint ck_booking_terms_duration check (duration_minutes > 0),
    constraint ck_booking_terms_latitude check (latitude between -90 and 90),
    constraint ck_booking_terms_longitude check (longitude between -180 and 180)
);

create index ix_booking_terms_public on booking_terms (starts_at asc, created_at desc);
create index ix_booking_terms_owner on booking_terms (owner_user_id, starts_at asc);

create table booking_reservations (
    id uuid primary key,
    term_id uuid not null references booking_terms(id) on delete cascade,
    participant_user_id uuid references user_accounts(id) on delete set null,
    first_name varchar(80) not null,
    last_name varchar(80) not null,
    email varchar(320) not null,
    phone_number varchar(40) not null,
    status varchar(40) not null,
    waitlist_position integer not null,
    cancellation_token varchar(64) not null unique,
    waitlist_confirmation_token varchar(64) unique,
    waitlist_offer_expires_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint ck_booking_reservations_waitlist_position check (waitlist_position >= 0)
);

create index ix_booking_reservations_term_status on booking_reservations (term_id, status);
create index ix_booking_reservations_waitlist_order on booking_reservations (term_id, waitlist_position, created_at);

create unique index ux_booking_reservations_active_email
    on booking_reservations (term_id, email)
    where status in ('CONFIRMED', 'WAITLISTED', 'WAITLIST_OFFERED');
