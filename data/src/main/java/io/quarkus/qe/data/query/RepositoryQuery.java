package io.quarkus.qe.data.query;

import java.util.stream.Stream;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import io.quarkus.qe.data.RepositoryEntity;

public class RepositoryQuery {

    public static final String FILTER_BRANCH = "byBranch";
    public static final String FILTER_RELATIVE_PATH = "byRelativePath";
    public static final String FILTER_REPO_URL = "byRepoUrl";

    public static final String PARAM = "param";

    private PanacheQuery<RepositoryEntity> query;

    private RepositoryQuery(PanacheQuery<RepositoryEntity> query) {
        this.query = query;
    }

    public RepositoryQuery filterByRepoUrl(String repoUrl) {
        return filterBy(repoUrl, FILTER_REPO_URL);
    }

    public RepositoryQuery filterByBranch(String branch) {
        return filterBy(branch, FILTER_BRANCH);
    }

    public RepositoryQuery filterByRelativePath(String relativePath) {
        return filterBy(relativePath, FILTER_RELATIVE_PATH);
    }

    public RepositoryQuery filterBy(String value, String filterDefinitionName) {
        if (value != null) {
            query = query.filter(filterDefinitionName, Parameters.with(PARAM, value));
        }

        return this;
    }

    public Stream<RepositoryEntity> stream() {
        return query.stream();
    }

    public long count() {
        return query.count();
    }

    public static final RepositoryQuery findAll() {
        return new RepositoryQuery(RepositoryEntity.<RepositoryEntity> findAll());
    }

    public static final RepositoryQuery findByRepoUrl(String repoUrl) {
        return new RepositoryQuery(RepositoryEntity.find("repoUrl", repoUrl));
    }
}
