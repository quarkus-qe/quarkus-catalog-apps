package io.quarkus.qe.enricher;

import io.quarkus.qe.model.Repository;

public interface Enricher {
    void enrichRepository(Repository repository);
}
