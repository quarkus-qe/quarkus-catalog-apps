package io.quarkus.qe.consumers;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.configuration.Channels;
import io.quarkus.qe.consumers.utils.InMemoryKafkaResource;
import io.quarkus.qe.consumers.utils.RepositoryEntityUtils;
import io.quarkus.qe.data.RepositoryEntity;
import io.quarkus.qe.data.marshallers.RepositoryMarshaller;
import io.quarkus.qe.model.Repository;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySource;

@QuarkusTest
@QuarkusTestResource(InMemoryKafkaResource.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
public class UpdateRepositoryRequestConsumerTest {

    private static final String REPO_URL = "http://github.com/user/repo.git";
    private static final String NEW_UPDATE = "The Update";

    @Inject
    @Any
    InMemoryConnector connector;

    @Inject
    RepositoryEntityUtils repositoryEntityUtils;

    @Inject
    RepositoryMarshaller repositoryMarshaller;

    private RepositoryEntity entity;
    private Repository repository;
    private InMemorySource<Repository> requests;

    @BeforeEach
    public void setup() {
        requests = connector.source(Channels.UPDATE_REPOSITORY);
        repositoryEntityUtils.deleteAll();
        entity = repositoryEntityUtils.create(REPO_URL);
    }

    @Test
    public void shouldUpdateRepository() {
        givenRepositoryWith(NEW_UPDATE);
        whenSendUpdate();
        thenUpdateIsStored();
    }

    private void givenRepositoryWith(String someUpdate) {
        repository = repositoryMarshaller.fromEntity(entity);
        repository.setSomeUpdate(someUpdate);
    }

    private void whenSendUpdate() {
        requests.send(repository);
    }

    private void thenUpdateIsStored() {
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(
                () -> assertEquals(NEW_UPDATE, repositoryEntityUtils.findById(repository.getId()).someUpdate));
    }

}
