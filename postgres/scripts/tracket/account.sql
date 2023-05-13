create table tracket.account
(
    id            serial
        constraint pk_account
            primary key,
    name          varchar(255)            not null,
    currency      char(4)                 not null
        constraint fk_currency
            references tracket.currency,
    creation_ts   timestamp default CURRENT_TIMESTAMP,
    account_type  varchar(255)            not null
        constraint fk_account_type
            references tracket.code_lookup (lookup_value),
    description   text,
    cash_in_cents integer   default 0     not null,
    is_deleted    boolean   default false not null
);

comment on table tracket.account is 'Accounts';

comment on column tracket.account.name is 'Name of account';

comment on column tracket.account.currency is 'Currency of money in account';

comment on constraint fk_currency on tracket.account is 'Only have ISO 4217 compliant currency codes';

comment on column tracket.account.creation_ts is 'Account creation timestamp';

comment on column tracket.account.account_type is 'Account type. Investment, Spending, etc...';

comment on column tracket.account.description is 'Short description of account';

comment on column tracket.account.cash_in_cents is 'Cash held in account (in cents)';

comment on column tracket.account.is_deleted is 'Flag for account soft deletion';

alter table tracket.account
    owner to postgres;

