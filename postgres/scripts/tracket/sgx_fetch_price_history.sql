create table tracket.sgx_fetch_price_history
(
    id                serial
        constraint pk_sgx_fetch_price_history_id
            primary key,
    path_code         integer not null,
    date_of_path_code date    not null,
    fetch_ts          timestamp default CURRENT_TIMESTAMP,
    file_blob         bytea
);

comment on table tracket.sgx_fetch_price_history is 'Stores the result and file of fetched SGX historical close prices';

comment on column tracket.sgx_fetch_price_history.path_code is 'SGX historical prices are of form http://<API_URL>/<path_code>/<file_name>. This stores the path_code that was retrieved in the run';

comment on column tracket.sgx_fetch_price_history.date_of_path_code is 'Each path code of the SGX API is related to a closing date';

comment on column tracket.sgx_fetch_price_history.fetch_ts is 'Timestamp of the fetch';

comment on column tracket.sgx_fetch_price_history.file_blob is 'Stores file retrieved';

alter table tracket.sgx_fetch_price_history
    owner to postgres;

