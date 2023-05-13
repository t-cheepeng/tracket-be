create table tracket.ticker_api
(
    id            serial
        constraint pk_ticker
            primary key,
    ticker_symbol varchar(20)   not null,
    api           varchar(255)  not null
        constraint fk_api
            references tracket.code_lookup (lookup_value),
    name          varchar(2500) not null
        constraint fk_name
            references tracket.stock,
    constraint uk_ticker_symbol_api
        unique (ticker_symbol, api)
);

comment on table tracket.ticker_api is 'All ticker to api mapping';

comment on column tracket.ticker_api.ticker_symbol is 'Symbol of ticker at a specific API';

comment on column tracket.ticker_api.api is 'The api to use for the ticker symbol';

comment on constraint fk_api on tracket.ticker_api is 'API -> lookup value';

comment on column tracket.ticker_api.name is 'Name of the stock';

comment on constraint fk_name on tracket.ticker_api is 'FK from name to stock.name';

comment on constraint uk_ticker_symbol_api on tracket.ticker_api is 'Unique pair (ticker_symbol, api)';

alter table tracket.ticker_api
    owner to postgres;

