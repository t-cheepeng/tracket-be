alter table tracket.account_transactions
    add exchange_rate_in_milli integer default 100000;

comment on column tracket.account_transactions.exchange_rate_in_milli is 'Exchange rate FROM to TO in millicents';

