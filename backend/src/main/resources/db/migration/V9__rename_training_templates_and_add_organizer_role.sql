alter table booking_training_enrollments
    rename to booking_training_templates;

alter index ix_booking_training_enrollments_owner
    rename to ix_booking_training_templates_owner;

alter table booking_training_templates
    rename constraint ck_booking_training_enrollments_capacity to ck_booking_training_templates_capacity;
alter table booking_training_templates
    rename constraint ck_booking_training_enrollments_cancellation_days to ck_booking_training_templates_cancellation_days;
alter table booking_training_templates
    rename constraint ck_booking_training_enrollments_duration to ck_booking_training_templates_duration;
alter table booking_training_templates
    rename constraint ck_booking_training_enrollments_latitude to ck_booking_training_templates_latitude;
alter table booking_training_templates
    rename constraint ck_booking_training_enrollments_longitude to ck_booking_training_templates_longitude;
alter table booking_training_templates
    rename constraint ck_booking_training_enrollments_training_level to ck_booking_training_templates_training_level;

drop index ix_booking_training_templates_owner;
create index ix_booking_training_templates_owner
    on booking_training_templates (owner_user_id, updated_at desc);

alter table booking_training_templates
    add column default_start_time time not null default time '07:00';
alter table booking_training_templates
    alter column default_start_time drop default;

alter table booking_training_templates
    drop constraint ck_booking_training_templates_capacity,
    add constraint ck_booking_training_templates_capacity check (capacity between 1 and 10),
    drop constraint ck_booking_training_templates_cancellation_days,
    add constraint ck_booking_training_templates_cancellation_days check (cancellation_deadline_days between 0 and 365),
    drop constraint ck_booking_training_templates_duration,
    add constraint ck_booking_training_templates_duration check (
        duration_minutes between 30 and 1440
            and duration_minutes % 30 = 0
        ),
    drop constraint ck_booking_training_templates_latitude,
    add constraint ck_booking_training_templates_latitude check (latitude between -90 and 90),
    drop constraint ck_booking_training_templates_longitude,
    add constraint ck_booking_training_templates_longitude check (longitude between -180 and 180),
    drop constraint ck_booking_training_templates_training_level,
    add constraint ck_booking_training_templates_training_level check (
        training_level in ('BASIC', 'INTERMEDIATE', 'ADVANCED')
        ),
    add constraint ck_booking_training_templates_default_start_time check (
        extract(second from default_start_time) = 0
            and extract(minute from default_start_time)::integer % 15 = 0
        );

insert into roles (name)
values ('ORGANIZER')
on conflict do nothing;
