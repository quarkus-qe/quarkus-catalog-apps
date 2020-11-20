package io.quarkus.qe.data.marshallers;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.qe.data.RepositoryEntity;
import io.quarkus.qe.model.Repository;

@ApplicationScoped
public class RepositoryMarshaller {

    @Inject
    QuarkusExtensionMarshaller quarkusExtensionMarshaller;

    public Repository fromEntity(RepositoryEntity entity) {
        Repository model = new Repository();
        model.setId(entity.id);
        model.setRepoUrl(entity.repoUrl);
        model.setBranch(entity.branch);
        model.setName(entity.name);

        if (entity.extensions != null) {
            entity.extensions.stream().map(quarkusExtensionMarshaller::fromEntity).forEach(model.getExtensions()::add);
        }

        return model;
    }

}
