package io.quarkus.qe.consumers;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import io.quarkus.qe.configuration.Channels;
import io.quarkus.qe.data.RepositoryEntity;
import io.quarkus.qe.model.Repository;
import io.smallrye.reactive.messaging.annotations.Blocking;

@ApplicationScoped
public class UpdateRepositoryRequestConsumer {

    private static final Logger LOG = Logger.getLogger(UpdateRepositoryRequestConsumer.class);

    @Incoming(Channels.UPDATE_REPOSITORY)
    @Blocking
    @Transactional
    public void addRepository(Repository repository) {
        LOG.info("Update repository " + repository.getRepoUrl());

        RepositoryEntity entity = RepositoryEntity.findById(repository.getId());
        entity.someUpdate = repository.getSomeUpdate();
        entity.persist();
    }
}
