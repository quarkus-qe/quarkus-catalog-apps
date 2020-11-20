package io.quarkus.qe.services.github;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.model.Repository;
import io.quarkus.qe.services.RepoUrlToRawService;

public class GitHubRepoUrlToRawServiceTest {

    private RepoUrlToRawService service;
    private String actualRawUrl;

    @BeforeEach
    public void setup() {
        service = new GitHubRepoUrlToRawService();
    }

    @Test
    public void shouldAppliesForGitHubOnly() {
        assertTrue(service.isFor(repository("https://github.com/repo/user")));
    }

    @Test
    public void shouldNotToApplyForGitLab() {
        assertFalse(service.isFor(repository("https://gitlab.com/repo/user")));
    }

    @Test
    public void shouldParseGitHubUrl() {
        whenGetRawUrl(repository("https://github.com/repo/user.git", "branch_name"));
        thenRawUrlIs("https://raw.githubusercontent.com/repo/user/branch_name");
    }

    private void whenGetRawUrl(Repository repository) {
        actualRawUrl = service.getRawUrl(repository);
    }

    private void thenRawUrlIs(String expectedRawUrl) {
        assertEquals(expectedRawUrl, actualRawUrl);
    }

    private Repository repository(String repoUrl) {
        Repository repository = new Repository();
        repository.setRepoUrl(repoUrl);
        return repository;
    }

    private Repository repository(String repoUrl, String branch) {
        Repository repository = repository(repoUrl);
        repository.setBranch(branch);
        return repository;
    }

}
