create table tracket.account_transactions
(
    id               serial
        constraint account_transactions_pk
            primary key,
    transaction_ts   timestamp default CURRENT_TIMESTAMP,
    account_id_from  integer      not null
        constraint fk_account_id_from
            references tracket.account,
    account_id_to    integer
        constraint fk_account_id_to
            references tracket.account,
    amount_in_cents  integer      not null,
    transaction_type varchar(255) not null
        constraint fk_transaction_type
            references tracket.code_lookup (lookup_value)
);

comment on table tracket.account_transactions is 'Keeps track of transactions of accounts and between accounts. Unreleated to actual trades';

comment on column tracket.account_transactions.id is 'Transaction id';

comment on column tracket.account_transactions.transaction_ts is 'timestamp of transaction';

comment on column tracket.account_transactions.account_id_from is 'Account related to transaction sending end';

comment on column tracket.account_transactions.account_id_to is 'account related to transaction receiving end';

comment on column tracket.account_transactions.amount_in_cents is 'Amounts transacted in cents';

comment on column tracket.account_transactions.transaction_type is 'Type of transaction';

alter table tracket.account_transactions
    owner to postgres;

