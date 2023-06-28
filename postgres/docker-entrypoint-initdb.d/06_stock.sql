create table tracket.stock
(
    name                  varchar(2500)         not null
        constraint pk_stock
            primary key,
    currency              char(4)               not null
        constraint fk_currency
            references tracket.currency,
    asset_class           varchar(255)          not null
        constraint fk_asset_class
            references tracket.code_lookup (lookup_value),
    is_deleted            boolean default false not null,
    display_ticker_symbol varchar(20)           not null
);

comment on column tracket.stock.name is 'Full name of the stock';

comment on column tracket.stock.is_deleted is 'Soft delete column';

comment on column tracket.stock.display_ticker_symbol is 'For displaying ticker symbol';

alter table tracket.stock
    owner to "tracket-be";

