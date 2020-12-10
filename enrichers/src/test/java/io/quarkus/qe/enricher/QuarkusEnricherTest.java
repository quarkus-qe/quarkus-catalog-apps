package io.quarkus.qe.enricher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.exceptions.EnrichmentException;
import io.quarkus.qe.model.QuarkusExtension;
import io.quarkus.qe.model.Repository;
import io.quarkus.qe.services.RepoUrlToRawService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;

@QuarkusTest
public class QuarkusEnricherTest {

    private static final String REPO_URL = "https://myrepo.com/user/repo";
    private static final String RAW_URL = REPO_URL + "/raw";
    private static final String BRANCH = "main";

    private static final String VERSION_FROM_BOM = "versionFromBom/";
    private static final String VERSION_FROM_DEPENDENCY = "versionFromDependency/";
    private static final String VERSION_FROM_QUARKUS_MAVEN_PLUGIN = "versionFromQuarkusMavenPlugin/";
    private static final String VERSION_FROM_MULTI_MODULE = "multiModuleExample/";

    private static final String EXTENSION_FROM_RESTAPI_MODULE = "quarkus-smallrye-reactive-messaging-kafka";
    private static final String EXTENSION_FROM_ENRICHER_MODULE = "quarkus-rest-client";
    private static final String EXTENSION_FROM_STORAGE_MODULE = "quarkus-flyway";
    private static final String EXTENSION_FROM_PARENT = "quarkus-junit5";

    private static final String VERSION_1_10_FINAL = "1.10.0.Final";
    private static final String VERSION_1_9_FINAL = "1.9.1.Final";

    @InjectMock
    private MockableRepoUrlToRawService rawUrlService;

    @Inject
    private QuarkusEnricher enricher;

    private Repository repository;

    @BeforeEach
    public void setup() {
        repository = new Repository();
        repository.setRepoUrl(REPO_URL);
        repository.setBranch(BRANCH);
    }

    @Test
    public void shouldRaiseEnrichExceptionIfNotSupported() {
        givenRepositoryIsNotSupported();
        Assertions.assertThrows(EnrichmentException.class, this::whenEnrichRepository);
    }

    @Test
    public void shouldReturnExpectedExtensions() throws EnrichmentException {
        givenMultiModuleRepositoryExample();
        whenEnrichRepository();

        thenExpectedExtensionsAre(EXTENSION_FROM_ENRICHER_MODULE, EXTENSION_FROM_STORAGE_MODULE, EXTENSION_FROM_PARENT);
        thenExpectedRepositoryVersionIs(VERSION_1_10_FINAL);
    }

    @Test
    public void shouldUseRelativePath() throws EnrichmentException {
        givenMultiModuleRepositoryExample();
        givenRepositoryHasRelativePath("/enrichers");
        whenEnrichRepository();
        thenExpectedExtensionsAre(EXTENSION_FROM_ENRICHER_MODULE);
    }

    @Test
    public void shouldFindVersionFromBomVersion() throws EnrichmentException {
        givenVersionFromBomRepositoryExample();
        whenEnrichRepository();
        thenExpectedExtensionsCount(1);
        thenExpectedExtensionIsFound("quarkus-junit5", VERSION_1_10_FINAL);
        thenExpectedRepositoryVersionIs(VERSION_1_10_FINAL);
    }

    @Test
    public void shouldFindVersionFromDepVersion() throws EnrichmentException {
        givenVersionFromDependencyRepositoryExample();
        whenEnrichRepository();
        thenExpectedExtensionsCount(1);
        thenExpectedExtensionIsFound("quarkus-junit5", VERSION_1_9_FINAL);
        thenExpectedRepositoryVersionIs(VERSION_1_10_FINAL);
    }

    @Test
    public void shouldFindVersionFromQuarkusMavenPluginVersion() throws EnrichmentException {
        givenVersionFromQuarkusMavenPluginRepositoryExample();
        whenEnrichRepository();
        thenExpectedExtensionsCount(1);
        thenExpectedExtensionIsFound("quarkus-junit5", VERSION_1_10_FINAL);
        thenExpectedRepositoryVersionIs(VERSION_1_10_FINAL);
    }

    @Test
    public void shouldFindQuarkusVersionDependenciesMultiModuleExample() throws EnrichmentException {
        givenMultiModuleRepositoryExample();
        whenEnrichRepository();
        thenExpectedExtensionIsFound(EXTENSION_FROM_RESTAPI_MODULE, VERSION_1_9_FINAL);
        thenExpectedExtensionIsFound(EXTENSION_FROM_PARENT, VERSION_1_10_FINAL);
        thenExpectedExtensionIsFound(EXTENSION_FROM_STORAGE_MODULE, VERSION_1_10_FINAL);
        thenExpectedExtensionIsFound(EXTENSION_FROM_ENRICHER_MODULE, VERSION_1_10_FINAL);
        thenExpectedRepositoryVersionIs(VERSION_1_10_FINAL);
    }

    private void givenRepositoryHasRelativePath(String relativePath) {
        repository.setRelativePath(relativePath);
    }

    private void givenRepositoryIsNotSupported() {
        when(rawUrlService.isFor(repository)).thenReturn(false);
    }

    private void givenVersionFromBomRepositoryExample() {
        givenRepositoryExample(VERSION_FROM_BOM);
    }

    private void givenVersionFromDependencyRepositoryExample() {
        givenRepositoryExample(VERSION_FROM_DEPENDENCY);
    }

    private void givenVersionFromQuarkusMavenPluginRepositoryExample() {
        givenRepositoryExample(VERSION_FROM_QUARKUS_MAVEN_PLUGIN);
    }

    private void givenMultiModuleRepositoryExample() {
        givenRepositoryExample(VERSION_FROM_MULTI_MODULE);
    }

    private void givenRepositoryExample(String examplePath) {
        when(rawUrlService.isFor(repository)).thenReturn(true);

        URL pomFile = getClass().getClassLoader().getResource(examplePath);
        when(rawUrlService.getRawUrl(repository)).thenReturn("file:///" + pomFile.getFile());
    }

    private void whenEnrichRepository() throws EnrichmentException {
        enricher.enrichRepository(repository);
    }

    private void thenExpectedExtensionsAre(String... expectedExtensions) {
        List<String> actualExtensions = repository.getExtensions().stream().map(QuarkusExtension::getName)
                .collect(Collectors.toList());

        assertTrue(Stream.of(expectedExtensions).anyMatch(actualExtensions::contains));
    }

    private void thenExpectedExtensionsCount(int expectedCount) {
        assertEquals(expectedCount, repository.getExtensions().size());
    }

    private void thenExpectedExtensionIsFound(String expectedExtension, String expectedVersion) {
        Optional<QuarkusExtension> actual = repository.getExtensionByName(expectedExtension);
        assertTrue(actual.isPresent(), "Extension " + expectedExtension + " is not found");
        assertEquals(expectedVersion, actual.get().getVersion());
    }

    private void thenExpectedRepositoryVersionIs(String expectedVersion) {
        assertEquals(expectedVersion, repository.getQuarkusVersion());
    }

    @ApplicationScoped
    public static class MockableRepoUrlToRawService extends RepoUrlToRawService {

        @Override
        public boolean isFor(Repository repository) {
            return true;
        }

        @Override
        public String getRawUrl(Repository repository) {
            return RAW_URL;
        }
    }
}
