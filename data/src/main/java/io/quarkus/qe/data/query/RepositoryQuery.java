package io.quarkus.qe.data.query;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.qe.data.RepositoryEntity;
import io.quarkus.qe.model.QuarkusExtension;
import io.quarkus.qe.model.requests.RepositoryQueryRequest;

public class RepositoryQuery {

    public static final String FILTER_BRANCH = "byBranch";
    public static final String FILTER_RELATIVE_PATH = "byRelativePath";
    public static final String FILTER_REPO_URL = "byRepoUrl";
    public static final String FILTER_VERSION = "byVersion";

    public static final String PARAM = "param";

    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final Predicate<RepositoryEntity> ALWAYS_TRUE = r -> true;

    private Predicate<RepositoryEntity> postLoadFilters = ALWAYS_TRUE;

    private PanacheQuery<RepositoryEntity> query;

    private RepositoryQuery(PanacheQuery<RepositoryEntity> query) {
        this.query = query.page(Page.ofSize(DEFAULT_PAGE_SIZE));
    }

    public void filterByRequest(RepositoryQueryRequest request) {
        filterByRepoUrl(request.getRepoUrl());
        filterByBranch(request.getBranch());
        filterByRelativePath(request.getRelativePath());
        filterByVersion(request.getQuarkusVersion());
        filterByExtensions(request.getExtensions());
        filterByLabels(request.getLabels());
    }

    public RepositoryQuery filterByExtensions(List<QuarkusExtension> extensions) {
        if (extensions != null && !extensions.isEmpty()) {
            for (QuarkusExtension extension : extensions) {
                addPostFilter(RepositoryEntity.byAnyExtensionWithNameAndVersionIfNotEmpty(extension.getName(),
                        extension.getVersion()));
            }
        }

        return this;
    }

    private RepositoryQuery filterByLabels(List<String> labels) {
        if (labels != null && !labels.isEmpty()) {
            for (String label : labels) {
                addPostFilter(RepositoryEntity.byAnyLabel(label));
            }
        }

        return this;
    }

    public RepositoryQuery filterByVersion(String version) {
        return filterBy(version, FILTER_VERSION);
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

    private void addPostFilter(Predicate<RepositoryEntity> filter) {
        postLoadFilters = postLoadFilters.and(filter);
    }

    public Stream<RepositoryEntity> stream() {
        List<RepositoryEntity> repositories = new LinkedList<>();
        int pageCount = query.pageCount();
        for (int index = 0; index < pageCount; index++) {
            repositories.addAll(query.stream().filter(postLoadFilters).collect(Collectors.toList()));
            query = query.nextPage();
        }

        return repositories.stream();
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
