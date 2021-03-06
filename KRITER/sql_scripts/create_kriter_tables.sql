CREATE TABLE IF NOT EXISTS CATALOG (
    MAGASIN VARCHAR(100),
    RAYON VARCHAR(100),
    CATEGORIE_NIVEAU_1 TEXT,
    CATEGORIE_NIVEAU_2 TEXT,
    CATEGORIE_NIVEAU_3 TEXT,
    CATEGORIE_NIVEAU_4 TEXT,
    CATEGORIE_NIVEAU_5 TEXT,
    SKU VARCHAR(100),
    LIBELLE_PRODUIT TEXT,
    MARQUE VARCHAR(100),
    DESCRIPTION_LONGUEUR50 TEXT,
    DESCRIPTION_LONGUEUR80 TEXT,
    URL TEXT,
    LIEN_IMAGE TEXT,
    VENDEUR VARCHAR(100),
    ETAT VARCHAR(100),
    NB_DISTINCT_CAT5 INT,
    NB_DISTINCT_CAT4 INT,
    NB_DISTINCT_BRAND INT,
    NB_DISTINCT_BRAND_WITHOUT_DEFAULT INT,   
    NB_DISTINCT_VENDOR INT,
    NB_DISTINCT_MAGASIN INT,
    NB_DISTINCT_STATE INT,
    DISTINCT_VENDOR TEXT,
    DISTINCT_MAGASIN TEXT,
    DISTINCT_STATE TEXT,
    DISTINCT_CAT5 TEXT,
    DISTINCT_CAT4 TEXT,
    DISTINCT_BRAND TEXT,
    TF_DISTANCE_LIBELLE TEXT,
    TF_IDF_DISTANCE_LIBELLE TEXT,
    LEVENSHTEIN_DISTANCE_LIBELLE TEXT,
    TF_DISTANCE_DESCRIPTION80 TEXT,
    TF_IDF_DISTANCE_DESCRIPTION80 TEXT,
    LEVENSHTEIN_DISTANCE_DESCRIPTION80 TEXT,
    CDS_NB_DISTINCT_CAT5 INT,
    CDS_NB_DISTINCT_CAT4 INT,
    CDS_NB_DISTINCT_BRAND INT,
    CDS_NB_DISTINCT_BRAND_WITHOUT_DEFAULT INT,   
    CDS_NB_DISTINCT_VENDOR INT,
    CDS_NB_DISTINCT_MAGASIN INT,
    CDS_NB_DISTINCT_STATE INT,
    CDS_DISTINCT_VENDOR TEXT,
    CDS_DISTINCT_MAGASIN TEXT,
    CDS_DISTINCT_STATE TEXT,
    CDS_DISTINCT_CAT5 TEXT,
    CDS_DISTINCT_CAT4 TEXT,
    CDS_DISTINCT_BRAND TEXT,
    CDS_TF_DISTANCE_LIBELLE TEXT,
    CDS_TF_IDF_DISTANCE_LIBELLE TEXT,
    CDS_LEVENSHTEIN_DISTANCE_LIBELLE TEXT,
    CDS_TF_DISTANCE_DESCRIPTION80 TEXT,
    CDS_TF_IDF_DISTANCE_DESCRIPTION80 TEXT,
    CDS_LEVENSHTEIN_DISTANCE_DESCRIPTION80 TEXT,
    KRIT_SKU1 VARCHAR(100),
    KRIT_SKU2  VARCHAR(100),
    KRIT_SKU3  VARCHAR(100),
    KRIT_SKU4  VARCHAR(100),
    KRIT_SKU5  VARCHAR(100),
    KRIT_SKU6  VARCHAR(100),
    SKU1 VARCHAR(100),
    SKU2  VARCHAR(100),
    SKU3  VARCHAR(100),
    SKU4  VARCHAR(100),
    SKU5  VARCHAR(100),
    SKU6  VARCHAR(100),
    COUNTER INT,
    IS_IN_TFIDF_INDEX BOOLEAN,
    TO_FETCH BOOLEAN
) TABLESPACE mydbspace;


CREATE TABLE IF NOT EXISTS LINKING_SIMILAR_PRODUCTS (
SKU VARCHAR(100),
COUNTER INT
) TABLESPACE mydbspace

CREATE TABLE IF NOT EXISTS CDS_LINKING_SIMILAR_PRODUCTS (
SKU VARCHAR(100),
COUNTER INT
) TABLESPACE mydbspace

CREATE TABLE IF NOT EXISTS SIMILAR_PRODUCTS (
SKU VARCHAR(100),
SKU1 VARCHAR(100),
SKU2  VARCHAR(100),
SKU3  VARCHAR(100),
SKU4  VARCHAR(100),
SKU5  VARCHAR(100),
SKU6  VARCHAR(100)
) TABLESPACE mydbspace

CREATE TABLE IF NOT EXISTS CDS_SIMILAR_PRODUCTS (
SKU VARCHAR(100) NOT NULL UNIQUE,
SKU1 VARCHAR(100),
SKU2  VARCHAR(100),
SKU3  VARCHAR(100),
SKU4  VARCHAR(100),
SKU5  VARCHAR(100),
SKU6  VARCHAR(100),
SKU7  VARCHAR(100)
) TABLESPACE mydbspace

ALTER TABLE CATALOG add NB_DISTINCT_VENDOR INT;
ALTER TABLE CATALOG add NB_DISTINCT_MAGASIN INT;
ALTER TABLE CATALOG add NB_DISTINCT_STATE INT;
ALTER TABLE CATALOG add DISTINCT_VENDOR TEXT;
ALTER TABLE CATALOG add DISTINCT_MAGASIN TEXT;
ALTER TABLE CATALOG add DISTINCT_STATE TEXT;
ALTER TABLE CATALOG add NB_DISTINCT_CAT5 INT;
ALTER TABLE CATALOG add NB_DISTINCT_CAT4 INT;
ALTER TABLE CATALOG add NB_DISTINCT_BRAND INT;
ALTER TABLE CATALOG add NB_DISTINCT_BRAND_WITHOUT_DEFAULT INT;
ALTER TABLE CATALOG add DISTINCT_CAT5 TEXT;
ALTER TABLE CATALOG add DISTINCT_CAT4 TEXT;
ALTER TABLE CATALOG add DISTINCT_BRAND TEXT;
ALTER TABLE CATALOG add TF_DISTANCE_LIBELLE TEXT;
ALTER TABLE CATALOG add TF_IDF_DISTANCE_LIBELLE TEXT;
ALTER TABLE CATALOG add LEVENSHTEIN_DISTANCE_LIBELLE TEXT;
ALTER TABLE CATALOG add TF_DISTANCE_DESCRIPTION80 TEXT;
ALTER TABLE CATALOG add TF_IDF_DISTANCE_DESCRIPTION80 TEXT;
ALTER TABLE CATALOG add LEVENSHTEIN_DISTANCE_DESCRIPTION80 TEXT;


ALTER TABLE CATALOG add CDS_NB_DISTINCT_VENDOR INT;
ALTER TABLE CATALOG add CDS_NB_DISTINCT_MAGASIN INT;
ALTER TABLE CATALOG add CDS_NB_DISTINCT_STATE INT;
ALTER TABLE CATALOG add CDS_DISTINCT_VENDOR TEXT;
ALTER TABLE CATALOG add CDS_DISTINCT_MAGASIN TEXT;
ALTER TABLE CATALOG add CDS_DISTINCT_STATE TEXT;
ALTER TABLE CATALOG add CDS_NB_DISTINCT_CAT5 INT;
ALTER TABLE CATALOG add CDS_NB_DISTINCT_CAT4 INT;
ALTER TABLE CATALOG add CDS_NB_DISTINCT_BRAND INT;
ALTER TABLE CATALOG add CDS_NB_DISTINCT_BRAND_WITHOUT_DEFAULT INT;
ALTER TABLE CATALOG add CDS_DISTINCT_CAT5 TEXT;
ALTER TABLE CATALOG add CDS_DISTINCT_CAT4 TEXT;
ALTER TABLE CATALOG add CDS_DISTINCT_BRAND TEXT;
ALTER TABLE CATALOG add CDS_TF_DISTANCE_LIBELLE TEXT;
ALTER TABLE CATALOG add CDS_TF_IDF_DISTANCE_LIBELLE TEXT;
ALTER TABLE CATALOG add CDS_LEVENSHTEIN_DISTANCE_LIBELLE TEXT;
ALTER TABLE CATALOG add CDS_TF_DISTANCE_DESCRIPTION80 TEXT;
ALTER TABLE CATALOG add CDS_TF_IDF_DISTANCE_DESCRIPTION80 TEXT;
ALTER TABLE CATALOG add CDS_LEVENSHTEIN_DISTANCE_DESCRIPTION80 TEXT;

ALTER TABLE CATALOG add IS_IN_TFIDF_INDEX BOOLEAN;