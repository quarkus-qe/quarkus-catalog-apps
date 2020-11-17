package io.quarkus.qe;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.configuration.Channels;
import io.quarkus.qe.model.Repository;
import io.quarkus.qe.utils.InMemoryKafkaResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySink;
import io.smallrye.reactive.messaging.connectors.InMemorySource;

@QuarkusTest
@QuarkusTestResource(InMemoryKafkaResource.class)
public class DispatcherTest {

    private static final String REPO_URL = "https://github.com/quarkus-qe/quarkus-catalog-apps";
    private static final String EXPECTED_NAME = "quarkus-catalog-apps";

    @Inject
    @Any
    InMemoryConnector connector;

    private InMemorySource<Repository> requests;
    private InMemorySink<Repository> responses;
    private Repository repository;

    @BeforeEach
    public void setup() {
        requests = connector.source(Channels.ENRICH_REPOSITORY);
        responses = connector.sink(Channels.UPDATE_REPOSITORY);
    }

    @Test
    public void shouldRepositoryBeUpdated() {
        givenRepositoryRequest();
        whenSend();
        thenRepositoryShouldBeUpdatedWithName(EXPECTED_NAME);

    }

    private void thenRepositoryShouldBeUpdatedWithName(String expectedName) {
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            assertEquals(1, responses.received().size());
            assertEquals(expectedName, responses.received().get(0).getPayload().getName());
        });
    }

    private void givenRepositoryRequest() {
        repository = new Repository();
        repository.setRepoUrl(REPO_URL);
    }

    private void whenSend() {
        requests.send(repository);
    }
}
