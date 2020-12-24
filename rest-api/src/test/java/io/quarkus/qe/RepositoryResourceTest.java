package io.quarkus.qe;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.configuration.Channels;
import io.quarkus.qe.data.RepositoryEntity;
import io.quarkus.qe.exceptions.CatalogError;
import io.quarkus.qe.exceptions.RepositoryAlreadyExistsException;
import io.quarkus.qe.exceptions.RepositoryNotFoundException;
import io.quarkus.qe.model.Repository;
import io.quarkus.qe.model.requests.NewRepositoryRequest;
import io.quarkus.qe.utils.InMemoryKafkaResource;
import io.quarkus.qe.utils.RepositoryEntityUtils;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySink;

@QuarkusTest
@QuarkusTestResource(InMemoryKafkaResource.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
public class RepositoryResourceTest {

    private static final long NOT_FOUND_ENTITY_ID = 777;
    private static final int EXPECTED_ALL_REPOS_AMOUNT = 3;
    private static final String PATH = "/repository";
    private static final String BRANCH = "master";
    private static final String NO_RELATIVE_PATH = null;
    private static final String REPO_URL = "http://github.com/user/repo.git";
    private static final String REPO_URL_1 = "http://github.com/user/repo1.git";
    private static final String REPO_URL_2 = "http://github.com/user/repo2.git";
    private static final String EXPECTED_CONFLICT_ERROR_MSG = "Repository " + REPO_URL + " already exist.";
    private static final String EXPECTED_NOT_FOUND_ERROR_MSG = "Repository ID " + NOT_FOUND_ENTITY_ID + " not exist.";

    @Inject
    @Any
    InMemoryConnector connector;

    @Inject
    RepositoryEntityUtils repositoryEntityUtils;

    private NewRepositoryRequest repository;
    private RepositoryEntity entity;
    private Response response;
    private InMemorySink<NewRepositoryRequest> newRepositoryResponses;
    private InMemorySink<Repository> updateRepositoryResponses;

    @BeforeEach
    public void setup() {
        repositoryEntityUtils.deleteAll();
        newRepositoryResponses = connector.sink(Channels.NEW_REPOSITORY);
        newRepositoryResponses.clear();
        updateRepositoryResponses = connector.sink(Channels.ENRICH_REPOSITORY);
        updateRepositoryResponses.clear();
    }

    @Test
    public void shouldAddRepository() {
        givenNewRepositoryRequest(REPO_URL);
        whenAddNewRepository();
        thenResponseIsAccepted();
        thenNewRepositoryRequestIsSent();
    }

    @Test
    public void shouldUpdateRepository() {
        givenExistingRepository(REPO_URL);
        whenUpdateRepository();
        thenResponseIsAccepted();
        thenUpdateRepositoryRequestIsSent();
    }

    @Test
    public void shouldFailToAddRepositoryWhenRepoIsNull() {
        givenNewRepositoryRequest(null);
        whenAddNewRepository();
        thenResponseIsInvalidRequest();
    }

    @Test
    public void shouldAllowAddRepositoriesForDifferentBranch() {
        givenExistingRepository(REPO_URL, "one-branch");
        givenNewRepositoryRequest(REPO_URL, "another-branch");
        whenAddNewRepository();
        thenResponseIsAccepted();
    }

    @Test
    public void shouldAllowAddRepositoriesForRelativePath() {
        givenExistingRepository(REPO_URL, BRANCH, "/path1");
        givenNewRepositoryRequest(REPO_URL, BRANCH, "/path2");
        whenAddNewRepository();
        thenResponseIsAccepted();
    }

    @Test
    public void shouldRaiseConflictIfRepositoryWithNoRelativePathAlreadyExists() {
        givenExistingRepository(REPO_URL);
        givenNewRepositoryRequest(REPO_URL);
        whenAddNewRepository();
        thenResponseIsConflict();
        thenResponseErrorCodeIs(RepositoryAlreadyExistsException.UNIQUE_SERVICE_ERROR_ID);
        thenResponseErrorMessageIs(EXPECTED_CONFLICT_ERROR_MSG);
    }

    @Test
    public void shouldRaiseConflictIfRepositoryWithSameRelativePathAlreadyExists() {
        givenExistingRepository(REPO_URL, BRANCH, "/path");
        givenNewRepositoryRequest(REPO_URL, BRANCH, "/path");
        whenAddNewRepository();
        thenResponseIsConflict();
        thenResponseErrorCodeIs(RepositoryAlreadyExistsException.UNIQUE_SERVICE_ERROR_ID);
        thenResponseErrorMessageIs(EXPECTED_CONFLICT_ERROR_MSG);
    }

    @Test
    public void shouldReturnRepositoryNotFound() throws RepositoryNotFoundException {
        whenUpdateRepository();
        thenResponseIsNotFound();
        thenResponseErrorCodeIs(RepositoryNotFoundException.UNIQUE_SERVICE_ERROR_ID);
        thenResponseErrorMessageIs(EXPECTED_NOT_FOUND_ERROR_MSG);
    }

    @Test
    public void shouldGetRepositoryById() {
        givenExistingRepository(REPO_URL);
        whenGetRepository();
        thenResponseIsOk();
    }

    @Test
    public void shouldGetAllRepository() {
        givenExistingRepository(REPO_URL);
        givenExistingRepository(REPO_URL_1);
        givenExistingRepository(REPO_URL_2);
        whenGetAllRepositories(0, EXPECTED_ALL_REPOS_AMOUNT);
        thenResponseIsOk();
        thenResponseRepositoryAmountIs(EXPECTED_ALL_REPOS_AMOUNT);
    }

    @Test
    public void shouldReturnNotFoundWhenEntityDoesNotExist() {
        whenGetRepository();
        thenResponseIsNotFound();
        thenResponseErrorCodeIs(RepositoryNotFoundException.UNIQUE_SERVICE_ERROR_ID);
        thenResponseErrorMessageIs(EXPECTED_NOT_FOUND_ERROR_MSG);
    }

    private void givenExistingRepository(String repoUrl) {
        givenExistingRepository(repoUrl, BRANCH);
    }

    private void givenExistingRepository(String repoUrl, String branch) {
        givenExistingRepository(repoUrl, branch, NO_RELATIVE_PATH);
    }

    private void givenExistingRepository(String repoUrl, String branch, String relativePath) {
        entity = repositoryEntityUtils.create(repoUrl, branch, relativePath);
    }

    private void givenNewRepositoryRequest(String repoUrl) {
        givenNewRepositoryRequest(repoUrl, BRANCH);
    }

    private void givenNewRepositoryRequest(String repoUrl, String branch) {
        givenNewRepositoryRequest(repoUrl, branch, NO_RELATIVE_PATH);
    }

    private void givenNewRepositoryRequest(String repoUrl, String branch, String relativePath) {
        repository = new NewRepositoryRequest();
        repository.setRepoUrl(repoUrl);
        repository.setBranch(branch);
        repository.setRelativePath(relativePath);
    }

    private void whenAddNewRepository() {
        response = given().contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).and()
                .body(repository).when().post(PATH);
    }

    private void whenUpdateRepository() {
        long entityId = Optional.ofNullable(entity).map(e -> e.id).orElse(NOT_FOUND_ENTITY_ID);
        response = given().contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).and()
                .when().put(PATH + "/" + entityId);
    }

    private void whenGetRepository() {
        long entityId = Optional.ofNullable(entity).map(e -> e.id).orElse(NOT_FOUND_ENTITY_ID);
        response = given().accept(MediaType.APPLICATION_JSON).when().get(PATH + "/" + entityId);
    }

    private void whenGetAllRepositories(int from, int to) {
        var queryParams = String.format("?page=%d&size=%d", from, to);
        response = given().accept(MediaType.APPLICATION_JSON).when().get(PATH + queryParams);
    }

    private void thenNewRepositoryRequestIsSent() {
        assertEquals(1, newRepositoryResponses.received().size());
    }

    private void thenUpdateRepositoryRequestIsSent() {
        assertEquals(1, updateRepositoryResponses.received().size());
    }

    private void thenResponseIsAccepted() {
        response.then().statusCode(HttpStatus.SC_ACCEPTED);
    }

    private void thenResponseIsOk() {
        response.then().statusCode(HttpStatus.SC_OK);
    }

    private void thenResponseIsInvalidRequest() {
        response.then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    private void thenResponseIsNotFound() {
        response.then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    private void thenResponseIsConflict() {
        response.then().statusCode(HttpStatus.SC_CONFLICT);
    }

    private void thenResponseRepositoryAmountIs(int expectedAmount) {
        assertEquals(expectedAmount, response.as(Repository[].class).length);
    }

    private void thenResponseErrorCodeIs(int expectedCode) {
        assertEquals(expectedCode, response.as(CatalogError.class).getCode());
    }

    private void thenResponseErrorMessageIs(String expectedMsg) {
        assertEquals(expectedMsg, response.as(CatalogError.class).getMsg());
    }

}
