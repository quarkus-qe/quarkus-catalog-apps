ALTER TABLE repository DROP CONSTRAINT repository_repourl_key;

CREATE UNIQUE INDEX repository_unique_key ON repository(repoUrl, branch, COALESCE(relativePath, ''))