create table tracket.account_group
(
    id       serial
        constraint pk_account_group
            primary key,
    name     varchar(255) not null
        constraint uk_name
            unique,
    currency char(4)      not null
        constraint fk_currency
            references tracket.currency
);

comment on table tracket.account_group is 'Groups created in application. Used to group accounts together';

comment on column tracket.account_group.name is 'Name of the group';

comment on constraint uk_name on tracket.account_group is 'Name must be unique';

comment on column tracket.account_group.currency is 'Currency of the group to display';

alter table tracket.account_group
    owner to "tracket-be";

