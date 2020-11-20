package io.quarkus.qe.services.gitlab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.model.Repository;
import io.quarkus.qe.services.RepoUrlToRawService;

public class GitLabRepoUrlToRawServiceTest {

    private RepoUrlToRawService service;
    private String actualRawUrl;

    @BeforeEach
    public void setup() {
        service = new GitLabRepoUrlToRawService();
    }

    @Test
    public void shouldAppliesForGitLabOnly() {
        assertTrue(service.isFor(repository("https://gitlab.com/repo/user")));
    }

    @Test
    public void shouldNotToApplyForGitHub() {
        assertFalse(service.isFor(repository("https://github.com/repo/user")));
    }

    @Test
    public void shouldParseGitLabUrl() {
        whenGetRawUrl(repository("https://gitlab.com/repo/user", "branch_name"));
        thenRawUrlIs("https://gitlab.com/repo/user/-/raw/branch_name");
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
