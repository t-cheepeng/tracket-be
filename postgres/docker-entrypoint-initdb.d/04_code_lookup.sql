create table tracket.code_lookup
(
    id           serial
        constraint pk_code_lookup
            primary key,
    lookup_type  varchar(100) not null,
    lookup_value varchar(255) not null
        constraint unq_code_lookup_lookup_value_0
            unique,
    constraint unq_code_lookup_lookup_value
        unique (lookup_type, lookup_value)
);

comment on table tracket.code_lookup is 'Code lookup table for general use. Beware not to overuse. Only use it for things that do not change';

comment on column tracket.code_lookup.lookup_type is 'Domain of the lookup';

comment on column tracket.code_lookup.lookup_value is 'Value in the domain';

alter table tracket.code_lookup
    owner to "tracket-be";

