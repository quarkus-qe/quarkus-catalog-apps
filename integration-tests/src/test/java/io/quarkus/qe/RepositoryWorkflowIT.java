package io.quarkus.qe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.model.Repository;
import io.quarkus.qe.model.requests.NewRepositoryRequest;

public class RepositoryWorkflowIT extends BaseIT {

    private static final String PATH = "/repository";
    private static final String REPO_URL = "https://github.com/quarkus-qe/quarkus-catalog-apps";
    private static final String REPO_ENRICHER_RELATIVE_PATH = "/enrichers";
    private static final String BRANCH = "main";
    private static final String EXPECTED_QUARKUS_VERSION = "1.10.0.Final";
    private static final String LABEL = "my-label";
    private static final String COMPLETED_STATE = "COMPLETED";
    private static final String PENDING_STATE = "PENDING";
    private static final String EXPECTED_REPOSITORY_NAME_FROM_GITHUB_API = "quarkus-catalog-apps";
    private static final String EXPECTED_EXTENSION_IN_ENRICHER = "quarkus-rest-client";
    private static final String EXPECTED_EXTENSION_IN_RESTAPI = "quarkus-smallrye-graphql";

    @Test
    public void shouldCreateRepositoryAndPopulateRepository() {
        whenCreateNewRepository();
        thenRepositoryShouldBeCreatedInDatabase();

        thenRepositoryShouldBeUpdated();
        thenRepositoryShouldHaveExtensions(EXPECTED_EXTENSION_IN_ENRICHER, EXPECTED_EXTENSION_IN_RESTAPI);
    }

    @Test
    public void shouldCreateRepositoryUsingRelativePath() {
        whenCreateNewRepository(REPO_ENRICHER_RELATIVE_PATH);

        thenRepositoryShouldBeUpdatedWithRelativePath(REPO_ENRICHER_RELATIVE_PATH);
        thenRepositoryShouldHaveExtensions(EXPECTED_EXTENSION_IN_ENRICHER);
        thenRepositoryShouldNotHaveExtensions(EXPECTED_EXTENSION_IN_RESTAPI);
    }

    private void whenCreateNewRepository(String relativePath) {
        NewRepositoryRequest repository = new NewRepositoryRequest();
        repository.setRepoUrl(REPO_URL);
        repository.setBranch(BRANCH);
        repository.setRelativePath(relativePath);
        repository.setLabels(Arrays.asList(LABEL));
        givenRestApiService().body(repository).post(PATH).then().statusCode(HttpStatus.SC_ACCEPTED);
    }

    private void whenCreateNewRepository() {
        whenCreateNewRepository(null);
    }

    private void thenRepositoryShouldBeCreatedInDatabase() {
        awaitFor(() -> {
            Optional<Repository> actual = getRepositoryByRepoUrl();
            assertTrue(actual.isPresent());
            assertNotNull(actual.get().getCreatedAt());
            assertEquals(PENDING_STATE, actual.get().getStatus());
        });
    }

    private void thenRepositoryShouldBeUpdatedWithRelativePath(String expectedRelativePath) {
        awaitFor(() -> {
            Repository actual = getRepositoryByRepoUrl().get();
            assertEquals(expectedRelativePath, actual.getRelativePath());
            assertEquals(COMPLETED_STATE, actual.getStatus());
        });
    }

    private void thenRepositoryShouldBeUpdated() {
        awaitFor(() -> {
            Repository actual = getRepositoryByRepoUrl().get();
            assertEquals(EXPECTED_QUARKUS_VERSION, actual.getQuarkusVersion());
            // https://github.com/quarkus-qe/quarkus-catalog-apps/issues/20
            // assertEquals(EXPECTED_REPOSITORY_NAME_FROM_GITHUB_API, actual.getName());
            assertNotNull(actual.getExtensions());
            assertFalse(actual.getExtensions().isEmpty());
            assertEquals(1, actual.getLabels().size());
            assertEquals(LABEL, actual.getLabels().iterator().next());
            assertNotNull(actual.getUpdatedAt());
            assertEquals(COMPLETED_STATE, actual.getStatus());
        });
    }

    private void thenRepositoryShouldHaveExtensions(String... expectedExtensions) {
        awaitFor(() -> {
            Repository actual = getRepositoryByRepoUrl().get();
            assertTrue(Stream.of(expectedExtensions).allMatch(expected -> actual.getExtensionByName(expected).isPresent()));
        });
    }

    private void thenRepositoryShouldNotHaveExtensions(String... notExpectedExtensions) {
        awaitFor(() -> {
            Repository actual = getRepositoryByRepoUrl().get();
            assertTrue(Stream.of(notExpectedExtensions).noneMatch(expected -> actual.getExtensionByName(expected).isPresent()));
        });
    }

    private Optional<Repository> getRepositoryByRepoUrl() {
        Repository[] list = givenRestApiService().get(PATH).then().statusCode(HttpStatus.SC_OK).and().extract()
                .as(Repository[].class);

        return Stream.of(list).filter(repo -> REPO_URL.equals(repo.getRepoUrl())).findFirst();
    }
}
