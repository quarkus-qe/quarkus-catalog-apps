package io.quarkus.qe;

import io.quarkus.qe.configuration.Channels;
import io.quarkus.qe.enricher.Enricher;
import io.quarkus.qe.exceptions.EnrichmentException;
import io.quarkus.qe.model.Log;
import io.quarkus.qe.model.Repository;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class Dispatcher {

    private static final Logger LOG = Logger.getLogger(Dispatcher.class);

    @Inject
    Instance<Enricher> enrichers;

    @Incoming(Channels.ENRICH_REPOSITORY)
    @Outgoing(Channels.UPDATE_REPOSITORY)
    public Repository dispatchRepository(Repository repository) {
        for (Enricher enricher : enrichers) {
            try {
                enricher.enrichRepository(repository);
            } catch (EnrichmentException exception) {
                LOG.error("Error running enricher " + enricher.type(), exception);
                repository.addLog(Log.error("Enricher '%s' failed. Caused by: %s", enricher.type(), exception.getMessage()));
            }
        }

        repository.addLog(Log.info("Repository enrichment finished."));

        return repository;
    }
}
