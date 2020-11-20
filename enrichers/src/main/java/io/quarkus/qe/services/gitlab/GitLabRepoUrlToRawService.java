package io.quarkus.qe.services.gitlab;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.qe.model.Repository;
import io.quarkus.qe.services.RepoUrlToRawService;

@ApplicationScoped
public class GitLabRepoUrlToRawService extends RepoUrlToRawService {

    private static final String GITLAB_HOST = "gitlab.com";

    @Override
    public boolean isFor(Repository repository) {
        return repository.getRepoUrl().contains(GITLAB_HOST);
    }

    @Override
    public String getRawUrl(Repository repository) {
        String repoUrl = removeGitExtension(repository.getRepoUrl());
        return repoUrl + String.format("/-/raw/%s", getBranchOrDefault(repository));
    }

}
