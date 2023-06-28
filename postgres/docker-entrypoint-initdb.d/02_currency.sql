create table tracket.currency
(
    code char(4) not null
        constraint pk_currency
            primary key
);

comment on table tracket.currency is 'ISO 4217 compliant currency table';

comment on column tracket.currency.code is 'Currency Code. E.g. SGD,USD';

alter table tracket.currency
    owner to "tracket-be";

