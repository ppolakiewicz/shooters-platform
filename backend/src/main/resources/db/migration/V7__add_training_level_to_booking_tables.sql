alter table booking_training_enrollments
    add column training_level varchar(20) not null default 'BASIC';

alter table booking_training_enrollments
    add constraint ck_booking_training_enrollments_training_level
        check (training_level in ('BASIC', 'INTERMEDIATE', 'ADVANCED'));

alter table booking_terms
    add column training_level varchar(20) not null default 'BASIC';

alter table booking_terms
    add constraint ck_booking_terms_training_level
        check (training_level in ('BASIC', 'INTERMEDIATE', 'ADVANCED'));
