CREATE SEQUENCE hibernate_sequence START 1;

CREATE TABLE repository
(
  id            BIGINT PRIMARY KEY,
  repourl       VARCHAR(100) NOT NULL UNIQUE,
  name          VARCHAR(100),
  branch        VARCHAR(50)
);