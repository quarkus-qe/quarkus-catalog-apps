package io.quarkus.qe.enricher;

import io.quarkus.qe.exceptions.EnrichmentException;
import io.quarkus.qe.model.Repository;

public interface Enricher {
    String type();

    void enrichRepository(Repository repository) throws EnrichmentException;
}
