package io.quarkus.qe.enricher.maven;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;

public class DependencyContext extends BaseContext {

    private static final String POM_TYPE = "pom";

    private final String artifactId;
    private final String type;
    private final Optional<String> version;

    public DependencyContext(Dependency dependency, MavenContext mavenContext) {
        super(mavenContext);
        this.artifactId = getOrLookupProperty(dependency.getArtifactId());
        this.type = dependency.getType();
        this.version = Optional.ofNullable(getOrLookupProperty(dependency.getVersion()))
                .or(this::getVersionFromDependencyManagement);
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    public Optional<String> getVersion() {
        return version;
    }

    public boolean isPomType() {
        return StringUtils.equalsIgnoreCase(type, POM_TYPE);
    }

    private Optional<String> getVersionFromDependencyManagement() {
        return getContext().getDependencyFromDependencyManagement(getArtifactId())
                .map(DependencyContext::getVersion)
                .orElse(Optional.empty());
    }
}
