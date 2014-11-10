# x y size not created (optional), Label URL (optional)

CREATE INDEX ON NODES (url);
ALTER SEQUENCE nodes_pkey RESTART WITH 1

CREATE TABLE IF NOT EXISTS NODES (
    ID SERIAL PRIMARY KEY NOT NULL,
    LABEL VARCHAR(100),
    URL TEXT
) TABLESPACE mydbspace;

# name weight not created (optional)
CREATE TABLE IF NOT EXISTS EDGES (
    SOURCE INT,
    TARGET INT
) TABLESPACE mydbspace;
