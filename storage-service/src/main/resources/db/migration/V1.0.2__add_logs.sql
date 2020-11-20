CREATE TABLE log
(
  id               BIGINT PRIMARY KEY,
  repository_id    BIGINT REFERENCES repository(id),
  level            VARCHAR(30) NOT NULL,
  message          VARCHAR(500) NOT NULL,
  timestamp        TIMESTAMP NOT NULL
);