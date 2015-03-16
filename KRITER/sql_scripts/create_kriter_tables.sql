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
    ETAT VARCHAR(100)
) TABLESPACE mydbspace;