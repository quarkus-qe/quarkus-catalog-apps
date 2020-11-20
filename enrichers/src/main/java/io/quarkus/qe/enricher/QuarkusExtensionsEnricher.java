package io.quarkus.qe.enricher;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import io.quarkus.qe.exceptions.EnrichmentException;
import io.quarkus.qe.model.QuarkusExtension;
import io.quarkus.qe.model.Repository;
import io.quarkus.qe.services.RepoUrlToRawService;

@ApplicationScoped
public class QuarkusExtensionsEnricher implements Enricher {

    private static final String QUARKUS_TAG = "quarkus-";
    private static final String POM_XML = "/pom.xml";

    @Inject
    Instance<RepoUrlToRawService> repoUrlToRawServices;

    @Override
    public String type() {
        return "Quarkus Extensions";
    }

    @Override
    public void enrichRepository(Repository repository) throws EnrichmentException {
        String rawUrl = getRawUrlFromRepository(repository);
        repository.setExtensions(findAllDependencies(rawUrl));
    }

    protected Model parseMavenModel(String baseUrl) throws EnrichmentException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try (InputStreamReader is = new InputStreamReader(new URL(baseUrl + POM_XML).openStream())) {
            return reader.read(is);
        } catch (IOException | XmlPullParserException e) {
            throw new EnrichmentException("Error reading the POM.xml at '%s': %s", baseUrl, e.getMessage());
        }
    }

    private String getRawUrlFromRepository(Repository repository) throws EnrichmentException {
        return repoUrlToRawServices.stream().filter(service -> service.isFor(repository))
                .map(service -> service.getRawUrl(repository)).findFirst()
                .orElseThrow(() -> new EnrichmentException("The %s repository is not supported. Can't get the RAW format. ",
                        repository.getRepoUrl()));
    }

    private Set<QuarkusExtension> findAllDependencies(String baseUrl) throws EnrichmentException {
        Set<QuarkusExtension> extensions = new HashSet<>();

        Model model = parseMavenModel(baseUrl);
        extensions.addAll(model.getDependencies().stream().map(Dependency::getArtifactId)
                .filter(artifact -> artifact.startsWith(QUARKUS_TAG))
                .map(this::toQuarkusExtensionModel)
                .collect(Collectors.toList()));

        for (String module : model.getModules()) {
            extensions.addAll(findAllDependencies(baseUrl + "/" + module));
        }

        return extensions;
    }

    private QuarkusExtension toQuarkusExtensionModel(String extension) {
        QuarkusExtension model = new QuarkusExtension();
        model.setName(extension);
        return model;
    }

}
