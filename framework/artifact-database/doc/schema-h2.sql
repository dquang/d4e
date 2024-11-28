--
-- schema to store artifacts in H2 databases.
--

BEGIN;

-- not using AUTO_INCREMENT to be more compatible with
-- other dbms.
CREATE SEQUENCE ARTIFACTS_ID_SEQ;

CREATE TABLE artifacts (
    id          INT PRIMARY KEY NOT NULL,
    gid         UUID            NOT NULL UNIQUE,
    creation    TIMESTAMP       NOT NULL,
    last_access TIMESTAMP       NOT NULL,
    ttl         BIGINT, -- NULL means eternal
    factory     VARCHAR(256)    NOT NULL,
    data        BINARY
);

CREATE SEQUENCE USERS_ID_SEQ;

CREATE TABLE users (
    id   INT PRIMARY KEY NOT NULL,
    gid  UUID            NOT NULL UNIQUE,
    name VARCHAR(256)    NOT NULL,
    account VARCHAR(256) NOT NULL UNIQUE,
    role BINARY
);

CREATE SEQUENCE COLLECTIONS_ID_SEQ;

CREATE TABLE collections (
    id          INT PRIMARY KEY NOT NULL,
    gid         UUID            NOT NULL UNIQUE,
    name VARCHAR(256)           NOT NULL,
    owner_id    INT             NOT NULL REFERENCES users(id),
    creation    TIMESTAMP       NOT NULL,
    last_access TIMESTAMP       NOT NULL,
    ttl         BIGINT, -- NULL means eternal
    attribute   BINARY
);

CREATE SEQUENCE COLLECTION_ITEMS_ID_SEQ;

CREATE TABLE collection_items (
    id            INT PRIMARY KEY NOT NULL,
    collection_id INT             NOT NULL REFERENCES collections(id),
    artifact_id   INT             NOT NULL REFERENCES artifacts(id),
    attribute     BINARY,
    creation      TIMESTAMP       NOT NULL,
    UNIQUE (collection_id, artifact_id)
);

CREATE TRIGGER collections_access_update_trigger AFTER UPDATE
    ON artifacts FOR EACH ROW
    CALL "org.dive4elements.artifactdatabase.h2.CollectionAccessUpdateTrigger";

COMMIT;
