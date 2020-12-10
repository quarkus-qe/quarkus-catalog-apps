package io.quarkus.qe.enricher;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import io.quarkus.qe.enricher.maven.MavenContext;
import io.quarkus.qe.enricher.quarkus.QuarkusExtensionsMavenPopulator;
import io.quarkus.qe.enricher.quarkus.QuarkusVersionMavenPopulator;
import io.quarkus.qe.exceptions.EnrichmentException;
import io.quarkus.qe.model.Repository;
import io.quarkus.qe.services.RepoUrlToRawService;

@ApplicationScoped
public class QuarkusEnricher implements Enricher {

    private static final String POM_XML = "/pom.xml";
    private static final String PATH = "/";

    @Inject
    Instance<RepoUrlToRawService> repoUrlToRawServices;

    @Inject
    QuarkusExtensionsMavenPopulator extensionsPopulator;

    @Inject
    QuarkusVersionMavenPopulator repositoryVersionPopulator;

    @Override
    public String type() {
        return "Quarkus";
    }

    @Override
    public final void enrichRepository(Repository repository) throws EnrichmentException {
        enrichRepositoryUsingRawUrl(repository, getRawUrlFromRepository(repository));
    }

    protected void enrichRepositoryUsingRawUrl(Repository repository, String rawUrl) throws EnrichmentException {
        MavenContext mavenContext = populateMavenContext(rawUrl);

        extensionsPopulator.populate(repository, mavenContext);
        repositoryVersionPopulator.populate(repository, mavenContext);
    }

    private MavenContext populateMavenContext(String baseUrl) throws EnrichmentException {
        MavenContext mavenContext = parseMavenContext(baseUrl);
        for (String module : mavenContext.getModules()) {
            mavenContext.addModuleContext(module, populateMavenContext(baseUrl + "/" + module));
        }

        return mavenContext;
    }

    protected MavenContext parseMavenContext(String baseUrl) throws EnrichmentException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try (InputStreamReader is = new InputStreamReader(new URL(baseUrl + POM_XML).openStream())) {
            return MavenContext.fromModel(reader.read(is));
        } catch (IOException | XmlPullParserException e) {
            throw new EnrichmentException("Error reading the POM.xml at '%s': %s", baseUrl, e.getMessage());
        }
    }

    private String getRawUrlFromRepository(Repository repository) throws EnrichmentException {
        return repoUrlToRawServices.stream().filter(service -> service.isFor(repository))
                .map(service -> service.getRawUrl(repository) + getRelativePath(repository))
                .findFirst().orElseThrow(throwsRepositoryNotSupported(repository));
    }

    private String getRelativePath(Repository repository) {
        if (StringUtils.isEmpty(repository.getRelativePath())) {
            return StringUtils.EMPTY;
        }

        String relativePath = repository.getRelativePath();
        if (!relativePath.startsWith(PATH)) {
            relativePath = PATH + relativePath;
        }

        return relativePath;
    }

    private Supplier<EnrichmentException> throwsRepositoryNotSupported(Repository repository) {
        return () -> new EnrichmentException("The %s repository is not supported. Can't get the RAW format. ",
                repository.getRepoUrl());
    }
}
