DROP TABLE IF EXISTS numbers;

create table numbers(
    number INTEGER PRIMARY KEY,
    citycode INTEGER NOT NULL,
    countrycode VARCHAR(2) NOT NULL,
    user_id VARCHAR(40) NOT NULL
);