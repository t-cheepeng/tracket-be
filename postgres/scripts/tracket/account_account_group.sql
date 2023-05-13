create table tracket.account_account_group
(
    account_id       integer not null
        constraint fk_account_id
            references tracket.account,
    account_group_id integer not null
        constraint fk_account_group_id
            references tracket.account_group,
    constraint unq_account_account_group
        unique (account_id, account_group_id)
);

comment on table tracket.account_account_group is 'Associative table between account and account_group.\n\nAccount can belong to 0 or more Groups\nGroups can have 0 or more accounts';

alter table tracket.account_account_group
    owner to postgres;

