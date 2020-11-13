package io.quarkus.qe.services;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import io.quarkus.qe.configuration.Channels;
import io.quarkus.qe.data.RepositoryEntity;
import io.quarkus.qe.data.marshallers.RepositoryMarshaller;
import io.quarkus.qe.exceptions.RepositoryAlreadyExistsException;
import io.quarkus.qe.exceptions.RepositoryNotFoundException;
import io.quarkus.qe.model.Repository;
import io.quarkus.qe.model.requests.NewRepositoryRequest;

@ApplicationScoped
public class RepositoryService {

    @Inject
    @Channel(Channels.NEW_REPOSITORY)
    Emitter<NewRepositoryRequest> newRepositoryRequestEmitter;

    @Inject
    RepositoryMarshaller repositoryMarshaller;

    public Repository findById(Long id) throws RepositoryNotFoundException {
        RepositoryEntity entity = RepositoryEntity.findById(id);
        if (entity == null) {
            throw new RepositoryNotFoundException();
        }

        return repositoryMarshaller.fromEntity(entity);
    }

    public void sendNewRepositoryRequest(Repository request) throws RepositoryAlreadyExistsException {
        if (RepositoryEntity.find("repoUrl", request.getRepoUrl()).count() > 0) {
            throw new RepositoryAlreadyExistsException();
        }

        newRepositoryRequestEmitter.send(new NewRepositoryRequest(request.getRepoUrl()));
    }
}