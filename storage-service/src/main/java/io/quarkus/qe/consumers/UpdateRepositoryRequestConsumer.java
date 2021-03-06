package io.quarkus.qe.consumers;

import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.qe.configuration.Channels;
import io.quarkus.qe.data.LogEntity;
import io.quarkus.qe.data.QuarkusExtensionEntity;
import io.quarkus.qe.data.QuarkusVersionEntity;
import io.quarkus.qe.data.RepositoryEntity;
import io.quarkus.qe.data.RepositoryStatus;
import io.quarkus.qe.data.marshallers.LogMarshaller;
import io.quarkus.qe.model.Log;
import io.quarkus.qe.model.QuarkusExtension;
import io.quarkus.qe.model.Repository;
import io.smallrye.reactive.messaging.annotations.Blocking;

@ApplicationScoped
public class UpdateRepositoryRequestConsumer {

    private static final Logger LOG = Logger.getLogger(UpdateRepositoryRequestConsumer.class);

    @Inject
    LogMarshaller logMarshaller;

    @Incoming(Channels.UPDATE_REPOSITORY)
    @Blocking
    @Transactional
    public void addRepository(Repository repository) {
        LOG.info("Update repository " + repository.getRepoUrl());

        RepositoryEntity entity = RepositoryEntity.findById(repository.getId());
        entity.name = repository.getName();
        entity.updatedAt = LocalDateTime.now();
        entity.status = RepositoryStatus.COMPLETED;
        entity.quarkusVersion = lookupQuarkusVersion(repository.getQuarkusVersion());
        updateQuarkusExtensions(repository, entity);
        updateLogs(repository, entity);
        entity.persist();
    }

    private void updateQuarkusExtensions(Repository repository, RepositoryEntity entity) {
        entity.extensions.clear();
        if (repository.getExtensions() != null) {
            for (QuarkusExtension extension : repository.getExtensions()) {
                QuarkusExtensionEntity extensionEntity = new QuarkusExtensionEntity();
                extensionEntity.repository = entity;
                extensionEntity.name = extension.getName();
                extensionEntity.version = lookupQuarkusVersion(extension.getVersion());
                entity.extensions.add(extensionEntity);
            }
        }
    }

    private void updateLogs(Repository repository, RepositoryEntity entity) {
        entity.logs.clear();
        entity.logs.forEach(PanacheEntityBase::delete);
        if (repository.getLogs() != null) {
            for (Log log : repository.getLogs()) {
                LogEntity logEntity = logMarshaller.fromModel(log);
                logEntity.repository = entity;
                entity.logs.add(logEntity);
            }
        }
    }

    private QuarkusVersionEntity lookupQuarkusVersion(String version) {
        if (version == null) {
            return null;
        }

        QuarkusVersionEntity versionEntity = QuarkusVersionEntity.findById(version);
        if (versionEntity == null) {
            versionEntity = new QuarkusVersionEntity();
            versionEntity.id = version;
            versionEntity.persist();
        }

        return versionEntity;
    }
}
