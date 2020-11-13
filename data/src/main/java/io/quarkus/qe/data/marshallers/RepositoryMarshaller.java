package io.quarkus.qe.data.marshallers;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.qe.data.RepositoryEntity;
import io.quarkus.qe.model.Repository;

@ApplicationScoped
public class RepositoryMarshaller {

    public Repository fromEntity(RepositoryEntity entity) {
        Repository model = new Repository();
        model.setId(entity.id);
        model.setRepoUrl(entity.repoUrl);
        return model;
    }

}
