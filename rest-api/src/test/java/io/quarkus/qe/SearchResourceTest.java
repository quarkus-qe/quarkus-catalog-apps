package io.quarkus.qe;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.Json;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Sets;

import io.quarkus.qe.data.RepositoryEntity;
import io.quarkus.qe.utils.InMemoryKafkaResource;
import io.quarkus.qe.utils.RepositoryEntityUtils;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

@QuarkusTest
@QuarkusTestResource(InMemoryKafkaResource.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
public class SearchResourceTest {

    private static final String PATH = "/graphql";
    private static final String REPO_URL = "http://github.com/user/repo.git";
    private static final String BRANCH = "master";
    private static final String NO_RELATIVE_PATH = null;

    @Inject
    RepositoryEntityUtils repositoryEntityUtils;

    private RepositoryEntity entity;
    private ValidatableResponse response;

    @BeforeEach
    public void setup() {
        repositoryEntityUtils.deleteAll();
    }

    @Test
    public void testRepositoriesQuery() {
        givenExistingRepository();
        whenRunGraphqlRepositories();
        thenRepositoryIsFound();
    }

    @Test
    public void testRepositoriesQueryFilterByQuarkusVersion() {
        givenExistingRepositoryWithVersion("1.7.1");
        whenRunGraphqlRepositoriesByVersionQuery("1.7.1");
        thenRepositoryIsFound();
    }

    @Test
    public void testRepositoriesQueryShouldReturnEmpty() {
        givenExistingRepositoryWithVersion("1.7.1");
        whenRunGraphqlRepositoriesByVersionQuery("version-not-found");
        thenRepositoryIsNotFound();
    }

    @Test
    public void testRepositoryByIdQuery() {
        givenExistingRepository();
        whenRunGraphqlRepositoryByIdQuery();
        thenRepositoryIsFound();
    }

    @Test
    public void testRepositoryByUrlQuery() {
        givenExistingRepository();
        whenRunGraphqlRepositoriesByUrlQuery();
        thenRepositoryIsFound();
    }

    @Test
    public void testRepositoryByUrlQueryAndBranch() {
        givenExistingRepository();
        whenRunGraphqlRepositoriesByUrlAndBranchQuery();
        thenRepositoryIsFound();
    }

    @Test
    public void testRepositoryByUrlQueryAndBranchIsNotFound() {
        givenExistingRepository();
        whenRunGraphqlRepositoriesByUrlAndBranchQuery("another-branch");
        thenRepositoryIsNotFound();
    }

    @Test
    public void testRepositoriesByExtensionsQuery() {
        givenExistingRepositoryWithExtensions("quarkus-a", "quarkus-b");
        whenRunGraphqlRepositoriesByExtensionQuery("quarkus-b");
        thenRepositoryIsFound();
    }

    @Test
    public void testRepositoriesByExtensionsQueryShouldReturnEmpty() {
        givenExistingRepositoryWithExtensions("quarkus-a", "quarkus-b");
        whenRunGraphqlRepositoriesByExtensionQuery("quarkus-not-found");
        thenRepositoryIsNotFound();
    }

    @Test
    public void testRepositoriesByLabelsQuery() {
        givenExistingRepositoryWithLabels("label-a", "label-b");
        whenRunGraphqlRepositoriesByLabelsQuery("label-b");
        thenRepositoryIsFound();
    }

    @Test
    public void testRepositoriesByLabelsQueryShouldReturnEmpty() {
        givenExistingRepositoryWithLabels("label-a", "label-b");
        whenRunGraphqlRepositoriesByLabelsQuery("not-found");
        thenRepositoryIsNotFound();
    }

    private void givenExistingRepository() {
        entity = repositoryEntityUtils.create(REPO_URL, BRANCH, NO_RELATIVE_PATH);
    }

    private void givenExistingRepositoryWithExtensions(String... extensions) {
        givenExistingRepository();
        repositoryEntityUtils.updateExtensions(entity.id, Sets.newHashSet(extensions));
    }

    private void givenExistingRepositoryWithLabels(String... labels) {
        givenExistingRepository();
        repositoryEntityUtils.updateLabels(entity.id, Sets.newHashSet(labels));
    }

    private void givenExistingRepositoryWithVersion(String version) {
        givenExistingRepository();
        repositoryEntityUtils.updateVersion(entity.id, version);
    }

    private void whenRunGraphqlRepositoriesByExtensionQuery(String name) {
        whenRunGraphqlRepositories("{ extensions: [{ name: \"" + name + "\"}]}");
    }

    private void whenRunGraphqlRepositoriesByLabelsQuery(String... labels) {
        whenRunGraphqlRepositories(
                "{ labels: [" + Stream.of(labels).map(l -> "\"" + l + "\"").collect(Collectors.joining(",")) + "]}");
    }

    private void whenRunGraphqlRepositoriesByUrlAndBranchQuery() {
        whenRunGraphqlRepositoriesByUrlAndBranchQuery(BRANCH);
    }

    private void whenRunGraphqlRepositoriesByUrlAndBranchQuery(String branch) {
        whenRunGraphqlRepositories("{ repoUrl: \"" + REPO_URL + "\", branch: \"" + branch + "\" }");
    }

    private void whenRunGraphqlRepositoriesByUrlQuery() {
        whenRunGraphqlRepositories("{ repoUrl: \"" + REPO_URL + "\" }");
    }

    private void whenRunGraphqlRepositoriesByVersionQuery(String quarkusVersion) {
        whenRunGraphqlRepositories("{ quarkusVersion: \"" + quarkusVersion + "\" }");
    }

    private void whenRunGraphqlRepositories() {
        whenRunGraphqlRepositories(null);
    }

    private void whenRunGraphqlRepositories(String filter) {
        String filterStr = "";
        if (filter != null) {
            filterStr = String.format("(request: %s)", filter);
        }

        whenRunGraphqlQuery(getPayload("{\n" +
                "  repositories " + filterStr + " {\n" +
                "    list {" +
                "       id\n" +
                "    }\n" +
                "  }\n" +
                "}"));
    }

    private void whenRunGraphqlRepositoryByIdQuery() {
        whenRunGraphqlQuery(getPayload("{\n" +
                "  repositoryById (id: " + entity.id + ") {\n" +
                "    id\n" +
                "  }\n" +
                "}"));

    }

    private void whenRunGraphqlQuery(String request) {
        response = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(request).post(PATH)
                .then().statusCode(HttpStatus.SC_OK)
                .and();
    }

    private void thenRepositoryIsFound() {
        response.body(containsString("\"id\":" + entity.id));
    }

    private void thenRepositoryIsNotFound() {
        response.body(not(containsString("\"id\":" + entity.id)));
    }

    private static String getPayload(String query) {
        return Json.createObjectBuilder().add("query", query).build().toString();
    }
}
