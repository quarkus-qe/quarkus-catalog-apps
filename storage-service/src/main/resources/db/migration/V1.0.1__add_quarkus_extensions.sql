CREATE TABLE quarkus_extension
(
  id               BIGINT PRIMARY KEY,
  repository_id    BIGINT REFERENCES repository(id),
  name             VARCHAR(200) NOT NULL
);