CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id              SERIAL,
    user_id         UUID NOT NULL,
    name            TEXT NOT NULL UNIQUE,
    password        VARCHAR(32)
    last_session    TIMESTAMP,
    CONSTRAINT user_id_pk PRIMARY KEY (id)
);