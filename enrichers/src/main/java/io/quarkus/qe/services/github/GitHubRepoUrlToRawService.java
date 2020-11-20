package io.quarkus.qe.services.github;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.qe.model.Repository;
import io.quarkus.qe.services.RepoUrlToRawService;

@ApplicationScoped
public class GitHubRepoUrlToRawService extends RepoUrlToRawService {

    private static final String GITHUB_HOST = "github.com";
    private static final String RAW_GITHUB_HOST = "raw.githubusercontent.com";

    @Override
    public boolean isFor(Repository repository) {
        return repository.getRepoUrl().contains(GITHUB_HOST);
    }

    @Override
    public String getRawUrl(Repository repository) {
        String repoUrl = removeGitExtension(repository.getRepoUrl())
                .replaceFirst(GITHUB_HOST, RAW_GITHUB_HOST);
        return repoUrl + String.format("/%s", getBranchOrDefault(repository));
    }
}
