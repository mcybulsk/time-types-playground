CREATE TABLE time_entity (
    id BIGINT PRIMARY KEY,

    instant_in_timestamp TIMESTAMP(3),
    instant_in_datetime DATETIME(3),

    local_date_time_in_timestamp TIMESTAMP(3),
    local_date_time_in_datetime DATETIME(3),

    date_in_timestamp TIMESTAMP(3),
    date_in_datetime DATETIME(3)
);
