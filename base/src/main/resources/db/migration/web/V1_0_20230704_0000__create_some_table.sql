-- create some table

CREATE SCHEMA IF NOT EXISTS ${schema};

CREATE TABLE IF NOT EXISTS ${schema}.quarkus
(
  id   uuid,
  name text,

  CONSTRAINT quarkus_pk PRIMARY KEY (id),
  CONSTRAINT quarkus_uk UNIQUE      (name)
);
