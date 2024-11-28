--
-- schema to store artifacts in PostgreSQL databases.
--

BEGIN;

-- not using AUTO_INCREMENT to be more compatible with
-- other dbms.
CREATE SEQUENCE ARTIFACTS_ID_SEQ;

CREATE TABLE artifacts (
    id          int PRIMARY KEY NOT NULL,
    gid         uuid            NOT NULL UNIQUE,
    creation    timestamp       NOT NULL,
    last_access timestamp       NOT NULL,
    ttl         bigint, -- NULL means eternal
    factory     VARCHAR(256)    NOT NULL,
    data        bytea
);

CREATE SEQUENCE USERS_ID_SEQ;

CREATE TABLE users (
    id   int PRIMARY KEY NOT NULL,
    gid  uuid            NOT NULL UNIQUE,
    name VARCHAR(256)    NOT NULL,
    account VARCHAR(256) NOT NULL UNIQUE,
    role bytea
);

CREATE SEQUENCE COLLECTIONS_ID_SEQ;

CREATE TABLE collections (
    id          int PRIMARY KEY NOT NULL,
    gid         uuid            NOT NULL UNIQUE,
    name VARCHAR(256)           NOT NULL,
    owner_id    int             NOT NULL REFERENCES users(id),
    creation    timestamp       NOT NULL,
    last_access timestamp       NOT NULL,
    ttl         bigint, -- NULL means eternal
    attribute   bytea
);

CREATE SEQUENCE COLLECTION_ITEMS_ID_SEQ;

CREATE TABLE collection_items (
    id            int PRIMARY KEY NOT NULL,
    collection_id int             NOT NULL REFERENCES collections(id),
    artifact_id   int             NOT NULL REFERENCES artifacts(id),
    attribute     bytea,
    creation      timestamp       NOT NULL,
    UNIQUE (collection_id, artifact_id)
);

CREATE FUNCTION collections_access_update() RETURNS trigger AS
$$
BEGIN
    UPDATE collections SET last_access = current_timestamp 
    WHERE id IN 
        (SELECT c.id FROM collections c 
         INNER JOIN collection_items ci ON c.id = ci.collection_id  
         INNER JOIN artifacts a         ON a.id = ci.artifact_id 
         WHERE a.id = NEW.id);
    RETURN NEW;
END;
$$
LANGUAGE 'plpgsql';


CREATE TRIGGER collections_access_update_trigger AFTER UPDATE
    ON artifacts FOR EACH ROW 
    EXECUTE PROCEDURE collections_access_update();

COMMIT;
