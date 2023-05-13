create table tracket.historical_stock_price
(
    id             serial
        constraint pk_price
            primary key,
    price_ts       timestamp     not null,
    price_in_cents integer       not null,
    name           varchar(2500) not null
        constraint fk_name
            references tracket.stock,
    api            varchar(255)  not null
        constraint fk_api
            references tracket.code_lookup (lookup_value)
);

comment on table tracket.historical_stock_price is 'Historical price table for stocks';

comment on column tracket.historical_stock_price.price_ts is 'Timestamp where the price is retrieved';

comment on column tracket.historical_stock_price.price_in_cents is 'Price of the stock in cents';

comment on column tracket.historical_stock_price.name is 'Name of the stock';

comment on column tracket.historical_stock_price.api is 'API used to retrieve the stock price';

comment on constraint fk_api on tracket.historical_stock_price is 'api -> lookup value api';

alter table tracket.historical_stock_price
    owner to postgres;

