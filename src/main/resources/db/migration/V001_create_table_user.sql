DROP TABLE IF EXISTS user;

CREATE TABLE user(
    id varchar(40) primary key not null,
    name varchar(100) null,
    email varchar(100) not null,
    password varchar(100) not null,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BIT NOT NULL DEFAULT 1,
    token varchar(400) not null
);