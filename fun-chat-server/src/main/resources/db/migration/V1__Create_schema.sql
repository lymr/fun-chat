CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    user_id     UUID NOT NULL UNIQUE,
    user_name   TEXT NOT NULL UNIQUE,
    last_seen   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE user_credentials (
    id          SERIAL PRIMARY KEY,
    user_id     UUID NOT NULL UNIQUE,
    password    VARCHAR(256),
    salt        VARCHAR(64),
    algorithm   VARCHAR(64),

    CONSTRAINT user_credentials_fk FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE CASCADE
)