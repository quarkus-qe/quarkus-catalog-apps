package io.quarkus.qe.enricher.maven;

import java.util.Optional;

import org.apache.maven.model.Plugin;

public class PluginContext extends BaseContext {

    private final String artifactId;
    private final Optional<String> version;

    public PluginContext(Plugin plugin, MavenContext mavenContext) {
        super(mavenContext);
        this.artifactId = getOrLookupProperty(plugin.getArtifactId());
        this.version = Optional.ofNullable(getOrLookupProperty(plugin.getVersion()))
                .or(this::getVersionFromPluginManagement);
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    public Optional<String> getVersion() {
        return version;
    }

    private Optional<String> getVersionFromPluginManagement() {
        return getContext().getPluginFromPluginManagement(getArtifactId())
                .map(PluginContext::getVersion)
                .orElse(Optional.empty());
    }
}
