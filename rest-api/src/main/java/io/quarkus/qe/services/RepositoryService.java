package io.quarkus.qe.services;

import io.quarkus.panache.common.Parameters;
import io.quarkus.qe.data.QuarkusVersionEntity;
import io.quarkus.qe.model.QuarkusExtension;
import java.util.*;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.quarkus.qe.configuration.Channels;
import io.quarkus.qe.data.RepositoryEntity;
import io.quarkus.qe.data.marshallers.RepositoryMarshaller;
import io.quarkus.qe.data.query.RepositoryQuery;
import io.quarkus.qe.exceptions.RepositoryAlreadyExistsException;
import io.quarkus.qe.exceptions.RepositoryNotFoundException;
import io.quarkus.qe.model.Repository;
import io.quarkus.qe.model.requests.NewRepositoryRequest;
import io.quarkus.qe.model.requests.RepositoryQueryRequest;

@ApplicationScoped
public class RepositoryService {

    @Inject
    @Channel(Channels.NEW_REPOSITORY)
    Emitter<NewRepositoryRequest> newRepositoryRequestEmitter;

    @Channel(Channels.ENRICH_REPOSITORY)
    Emitter<Repository> enrichEmitter;

    @Inject
    RepositoryMarshaller repositoryMarshaller;

    public Repository findById(Long id) throws RepositoryNotFoundException {
        RepositoryEntity entity = RepositoryEntity.findById(id);
        if (entity == null) {
            throw new RepositoryNotFoundException(String.format("Repository ID %d not exist.", id));
        }

        return repositoryMarshaller.fromEntity(entity);
    }

    public List<Repository> findByExtensionsArtifactIds(List<String> artifactIDs) {
        return RepositoryEntity.<RepositoryEntity> find(
                "select distinct repo from repository repo inner join fetch repo.extensions ext where ext.name IN :names",
                Parameters.with("names", artifactIDs)).stream()
                .map(repositoryMarshaller::fromEntity)
                .collect(Collectors.toList());
    }

    public List<Repository> findByExtensions(List<QuarkusExtension> extensions) {
        // compose query
        StringBuilder sql = new StringBuilder();
        sql.append("select distinct repo from repository repo inner join fetch repo.extensions ext\t");
        Iterator<QuarkusExtension> it = extensions.iterator();
        if (it.hasNext()) {
            QuarkusExtension extensionVersion = it.next();
            sql.append("where\t");
            String nameVersionStatement = "ext.name = '%s' and ext.version = '%s'\t";
            sql.append(String.format(nameVersionStatement, extensionVersion.getName(), extensionVersion.getVersion()));
            while (it.hasNext()) {
                extensionVersion = it.next();
                sql.append("or\t");
                sql.append(String.format(nameVersionStatement, extensionVersion.getName(), extensionVersion.getVersion()));
            }
        }

        // make query
        return RepositoryEntity.<RepositoryEntity> find(sql.toString())
                .stream()
                .map(repositoryMarshaller::fromEntity)
                .collect(Collectors.toList());
    }

    public List<Repository> find(RepositoryQueryRequest request) {
        RepositoryQuery query = RepositoryQuery.findAll();

        if (request != null) {
            query.filterByRepoUrl(request.getRepoUrl());
            query.filterByBranch(request.getBranch());
            query.filterByRelativePath(request.getRelativePath());
        }

        return query.stream()
                .map(repositoryMarshaller::fromEntity)
                .collect(Collectors.toList());
    }

    public List<Repository> findAll(int pageIndex, int size) {
        return RepositoryEntity.<RepositoryEntity> findAll()
                .page(pageIndex, size)
                .stream()
                .map(repositoryMarshaller::fromEntity)
                .collect(Collectors.toList());
    }

    public void sendNewRepositoryRequest(NewRepositoryRequest request) throws RepositoryAlreadyExistsException {
        RepositoryQuery query = RepositoryQuery.findByRepoUrl(request.getRepoUrl())
                .filterByBranch(request.getBranch())
                .filterByRelativePath(request.getRelativePath());
        if (query.count() > 0) {
            throw new RepositoryAlreadyExistsException(String.format("Repository %s already exist.", request.getRepoUrl()));
        }
        newRepositoryRequestEmitter.send(request);
    }

    public void updateRepositoryRequest(long id) throws RepositoryNotFoundException {
        enrichEmitter.send(findById(id));
    }

    private Map<String, Object> getQuarkusVersionAsQueryParam(String parameterName, List<String> versions) {
        Map<String, Object> params = new HashMap<>();
        List<QuarkusVersionEntity> versionsParam = versions.stream()
                .map(QuarkusVersionEntity::new)
                .filter(quarkusVersion -> Objects.nonNull(quarkusVersion.id))
                .collect(Collectors.toList());

        if (!versionsParam.isEmpty()) {
            params.put(parameterName, versionsParam);
        }

        return params;
    }
}
