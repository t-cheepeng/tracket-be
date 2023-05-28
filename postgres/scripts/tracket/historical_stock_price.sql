create table tracket.historical_stock_price
(
    id       serial
        constraint pk_price
            primary key,
    price_ts timestamp      not null,
    price    numeric(13, 6) not null,
    name     varchar(2500)  not null
        constraint fk_name
            references tracket.stock,
    api      varchar(255)   not null
        constraint fk_api
            references tracket.code_lookup (lookup_value)
);

comment on table tracket.historical_stock_price is 'Historical price table for stocks';

comment on column tracket.historical_stock_price.price_ts is 'Timestamp where the price is indicative. i.e. at this timestamp, the stock has this price';

comment on column tracket.historical_stock_price.price is 'Price of stock handling values from -9,999,999.999999 to +9,999,999.999999';

comment on column tracket.historical_stock_price.name is 'Name of the stock';

comment on column tracket.historical_stock_price.api is 'API used to retrieve the stock price';

comment on constraint fk_api on tracket.historical_stock_price is 'api -> lookup value api';

alter table tracket.historical_stock_price
    owner to postgres;

