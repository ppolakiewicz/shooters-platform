create table trainings (
    id uuid primary key,
    owner_user_id uuid not null references user_accounts(id) on delete cascade,
    name varchar(120) not null,
    place varchar(120) not null,
    description varchar(2048) not null,
    performed_on date not null,
    weapon_type varchar(32) not null,
    scoring_type varchar(32) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index ix_trainings_owner_performed on trainings (owner_user_id, performed_on desc, created_at desc);

create table shooting_tasks (
    id uuid primary key,
    training_id uuid not null references trainings(id) on delete cascade,
    run_number integer not null,
    weapon_type varchar(32) not null,
    scoring_type varchar(32) not null,
    duration_tenths integer not null,
    constraint uk_shooting_tasks_training_run unique (training_id, run_number),
    constraint ck_shooting_tasks_duration_positive check (duration_tenths > 0)
);

create table shooting_task_scores (
    id uuid primary key,
    shooting_task_id uuid not null references shooting_tasks(id) on delete cascade,
    score_key varchar(32) not null,
    hit_count integer not null,
    constraint uk_shooting_task_scores_task_key unique (shooting_task_id, score_key),
    constraint ck_shooting_task_scores_non_negative check (hit_count >= 0)
);
