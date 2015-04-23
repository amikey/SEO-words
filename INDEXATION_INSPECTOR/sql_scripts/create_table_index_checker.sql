CREATE TABLE IF NOT EXISTS URL_TO_CHECK_LIST (
    ID SERIAL PRIMARY KEY     NOT NULL,
    COMING_FROM VARCHAR(100),
    URL TEXT,
    TO_FETCH BOOLEAN,
    IN_INDEX BOOLEAN,
    LAST_UPDATE DATE 
) TABLESPACE mydbspace;
