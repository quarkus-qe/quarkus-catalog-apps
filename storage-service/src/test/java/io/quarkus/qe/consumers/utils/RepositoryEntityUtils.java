package io.quarkus.qe.consumers.utils;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import io.quarkus.qe.data.RepositoryEntity;

@ApplicationScoped
public class RepositoryEntityUtils {

    @Transactional
    public void deleteAll() {
        RepositoryEntity.deleteAll();
    }

    @Transactional
    public RepositoryEntity create(String repoUrl) {
        RepositoryEntity entity = new RepositoryEntity();
        entity.repoUrl = repoUrl;
        entity.persist();

        return entity;
    }

    @Transactional
    public RepositoryEntity findById(Long id) {
        return RepositoryEntity.<RepositoryEntity> findById(id);
    }
}
