CREATE TABLE IF NOT EXISTS SOLR_VS_EXALEAD_SEARCH_LIST (
    ID SERIAL PRIMARY KEY  NOT NULL,
    SEARCH_EXPRESSION VARCHAR(400),
    FACETTES_QUERY VARCHAR(400),
    PROVENANCE VARCHAR(100),
    TO_FETCH BOOLEAN,
    STATUS_SOLR INT,
    STATUS_EXALEAD INT,
    NB_PRODUCTS_SOLR TEXT,
    FACETTES_SOLR TEXT,
    LISTSKUS_SOLR TEXT,
    NB_PRODUCTS_EXALEAD TEXT,
    FACETTES_EXALEAD TEXT,
    LISTSKUS_EXALEAD TEXT
) TABLESPACE mydbspace;

