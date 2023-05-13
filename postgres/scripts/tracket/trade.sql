create table tracket.trade
(
    id             serial
        constraint pk_trade
            primary key,
    trade_ts       timestamp         not null,
    trade_type     varchar(255)      not null
        constraint fk_trade_type
            references tracket.code_lookup (lookup_value),
    num_of_units   integer           not null,
    price_per_unit integer           not null,
    name           varchar(2500)     not null
        constraint fk_name
            references tracket.stock,
    account        integer           not null
        constraint fk_account
            references tracket.account,
    fee            integer default 0 not null,
    buy_id         integer
        constraint fk_buy_id
            references tracket.trade
);

comment on table tracket.trade is 'Trades being tracked';

comment on column tracket.trade.trade_ts is 'Timestamp at which trade has happened';

comment on column tracket.trade.trade_type is 'Type of trade: E.g. Buy, Sell, Dividend, etc...';

comment on constraint fk_trade_type on tracket.trade is 'trade_type -> lookup table value';

comment on column tracket.trade.price_per_unit is 'Price per unit in cents';

comment on column tracket.trade.name is 'Name of the stock';

comment on constraint fk_name on tracket.trade is 'name -> stock name';

comment on column tracket.trade.account is 'Account in which the trade happened in';

comment on constraint fk_account on tracket.trade is 'Account -> account';

comment on column tracket.trade.fee is 'Fee in cents';

comment on column tracket.trade.buy_id is 'ID of the buy trade that is being sold';

comment on constraint fk_buy_id on tracket.trade is 'Buy ID must exist in table before stock can be sold';

alter table tracket.trade
    owner to postgres;

