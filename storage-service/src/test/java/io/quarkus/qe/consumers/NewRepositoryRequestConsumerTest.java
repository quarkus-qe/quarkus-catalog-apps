package io.quarkus.qe.consumers;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.configuration.Channels;
import io.quarkus.qe.consumers.utils.InMemoryKafkaResource;
import io.quarkus.qe.consumers.utils.RepositoryEntityUtils;
import io.quarkus.qe.data.RepositoryEntity;
import io.quarkus.qe.data.RepositoryStatus;
import io.quarkus.qe.model.Repository;
import io.quarkus.qe.model.requests.NewRepositoryRequest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySink;
import io.smallrye.reactive.messaging.connectors.InMemorySource;

@QuarkusTest
@QuarkusTestResource(InMemoryKafkaResource.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
public class NewRepositoryRequestConsumerTest {

    private static final String REPO_URL = "http://github.com/user/repo.git";
    private static final String BRANCH = "master";
    private static final String NO_RELATIVE_PATH = null;
    private static final String LABEL = "aLabel";

    @Inject
    @Any
    InMemoryConnector connector;

    @Inject
    RepositoryEntityUtils repositoryEntityUtils;

    private NewRepositoryRequest repository;
    private InMemorySource<NewRepositoryRequest> requests;
    private InMemorySink<Repository> responses;

    @BeforeEach
    public void setup() {
        repositoryEntityUtils.deleteAll();
        requests = connector.source(Channels.NEW_REPOSITORY);
        responses = connector.sink(Channels.ENRICH_REPOSITORY);
        responses.clear();
    }

    @Test
    public void shouldAddRepositoryAndThenSendToEnrich() {
        givenNewRepositoryRequest();
        whenSendNewRequest();
        thenResponseIsSent();
        thenRepositoryIsStored();
    }

    @Test
    public void shouldAllowAddRepositoriesForDifferentBranch() {
        givenExistingRepository("one-branch");
        givenNewRepositoryRequest("another-branch");
        whenSendNewRequest();
        thenResponseIsSent();
        thenRepositoryIsStored("another-branch");
    }

    @Test
    public void shouldAllowAddRepositoriesForRelativePath() {
        givenExistingRepository(BRANCH, "/path1");
        givenNewRepositoryRequest(BRANCH, "/path2");
        whenSendNewRequest();
        thenResponseIsSent();
        thenRepositoryIsStored(BRANCH, "/path2");
    }

    @Test
    public void shouldFailIfExistingRepository() {
        givenExistingRepository(BRANCH, "/path1");
        givenNewRepositoryRequest(BRANCH, "/path1");
        whenSendNewRequest();
        thenResponseIsNotSent();
    }

    private void givenExistingRepository(String branch) {
        givenExistingRepository(branch, NO_RELATIVE_PATH);
    }

    private void givenExistingRepository(String branch, String relativePath) {
        repositoryEntityUtils.create(REPO_URL, branch, relativePath);
    }

    private void givenNewRepositoryRequest() {
        givenNewRepositoryRequest(BRANCH);
    }

    private void givenNewRepositoryRequest(String branch) {
        givenNewRepositoryRequest(branch, NO_RELATIVE_PATH);
    }

    private void givenNewRepositoryRequest(String branch, String relativePath) {
        repository = new NewRepositoryRequest();
        repository.setRepoUrl(REPO_URL);
        repository.setBranch(branch);
        repository.setRelativePath(relativePath);
        repository.setLabels(Arrays.asList(LABEL));
    }

    private void whenSendNewRequest() {
        requests.send(repository);
    }

    private void thenRepositoryIsStored() {
        thenRepositoryIsStored(BRANCH);
    }

    private void thenRepositoryIsStored(String branch) {
        thenRepositoryIsStored(branch, NO_RELATIVE_PATH);
    }

    private void thenRepositoryIsStored(String branch, String relativePath) {
        List<RepositoryEntity> entities = RepositoryEntity.find("repoUrl", REPO_URL).list();
        if (branch != null) {
            entities = entities.stream().filter(r -> r.branch.equals(branch)).collect(Collectors.toList());
        }

        if (relativePath != null) {
            entities = entities.stream().filter(r -> r.relativePath.equals(relativePath)).collect(Collectors.toList());
        }

        assertEquals(1, entities.size());

        RepositoryEntity actualEntity = entities.get(0);
        assertEquals(1, actualEntity.labels.size());
        assertEquals(LABEL, actualEntity.labels.iterator().next().name);
        assertEquals(RepositoryStatus.PENDING, actualEntity.status);
        assertNotNull(actualEntity.createdAt);
    }

    private void thenResponseIsSent() {
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> assertEquals(1, responses.received().size()));
    }

    private void thenResponseIsNotSent() {
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> assertEquals(0, responses.received().size()));
    }

}
