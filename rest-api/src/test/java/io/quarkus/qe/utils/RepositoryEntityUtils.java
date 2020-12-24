package io.quarkus.qe.utils;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import io.quarkus.qe.data.LabelEntity;
import io.quarkus.qe.data.QuarkusExtensionEntity;
import io.quarkus.qe.data.QuarkusVersionEntity;
import io.quarkus.qe.data.RepositoryEntity;

@ApplicationScoped
public class RepositoryEntityUtils {

    @Transactional
    public void deleteAll() {
        LabelEntity.deleteAll();
        QuarkusExtensionEntity.deleteAll();
        RepositoryEntity.deleteAll();
    }

    @Transactional
    public RepositoryEntity create(String repoUrl, String branch, String relativePath) {
        RepositoryEntity entity = new RepositoryEntity();
        entity.repoUrl = repoUrl;
        entity.branch = branch;
        entity.relativePath = relativePath;
        entity.persist();

        return entity;
    }

    @Transactional
    public RepositoryEntity updateVersion(Long id, String version) {
        RepositoryEntity entity = RepositoryEntity.findById(id);
        QuarkusVersionEntity versionEntity = QuarkusVersionEntity.findById(version);
        if (versionEntity == null) {
            versionEntity = new QuarkusVersionEntity();
            versionEntity.id = version;
            versionEntity.persist();
        }

        entity.quarkusVersion = versionEntity;

        entity.persist();

        return entity;
    }

    @Transactional
    public RepositoryEntity updateExtensions(Long id, Set<String> extensions) {
        RepositoryEntity entity = RepositoryEntity.findById(id);
        entity.extensions.clear();

        for (String extensionName : extensions) {
            QuarkusExtensionEntity extensionEntity = new QuarkusExtensionEntity();
            extensionEntity.repository = entity;
            extensionEntity.name = extensionName;
            extensionEntity.version = entity.quarkusVersion;

            entity.extensions.add(extensionEntity);
        }

        entity.persist();

        return entity;
    }

    @Transactional
    public RepositoryEntity updateLabels(Long id, Set<String> labels) {
        RepositoryEntity entity = RepositoryEntity.findById(id);
        entity.labels.clear();

        for (String label : labels) {
            LabelEntity labelEntity = new LabelEntity();
            labelEntity.repository = entity;
            labelEntity.name = label;

            entity.labels.add(labelEntity);
        }

        entity.persist();

        return entity;
    }
}
