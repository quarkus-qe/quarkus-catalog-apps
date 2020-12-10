package io.quarkus.qe.enricher.quarkus;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import io.quarkus.qe.enricher.maven.DependencyContext;
import io.quarkus.qe.enricher.maven.MavenContext;
import io.quarkus.qe.enricher.maven.PluginContext;
import io.quarkus.qe.model.Repository;

public abstract class BaseQuarkusMavenPopulator {

    private static final String QUARKUS_UNIVERSE_BOM_ARTIFACT = "quarkus-universe-bom";
    private static final String QUARKUS_BOM_ARTIFACT = "quarkus-bom";
    private static final String QUARKUS_MAVEN_PLUGIN_ARTIFACT = "quarkus-maven-plugin";

    public abstract void populate(Repository repository, MavenContext mavenContext);

    protected String resolveVersionFromContext(MavenContext context) {
        return getVersionFromQuarkusUniverseBom(context)
                .or(() -> getVersionFromQuarkusBom(context))
                .or(() -> getVersionFromQuarkusPlugin(context))
                .orElse(StringUtils.EMPTY);
    }

    private Optional<String> getVersionFromQuarkusUniverseBom(MavenContext context) {
        return getVersionFromDependencyManagement(context, QUARKUS_UNIVERSE_BOM_ARTIFACT);
    }

    private Optional<String> getVersionFromQuarkusBom(MavenContext context) {
        return getVersionFromDependencyManagement(context, QUARKUS_BOM_ARTIFACT);
    }

    private Optional<String> getVersionFromDependencyManagement(MavenContext context, String artifactId) {
        return context.getDependencyFromDependencyManagement(artifactId)
                .filter(DependencyContext::isPomType)
                .flatMap(DependencyContext::getVersion);
    }

    private Optional<String> getVersionFromQuarkusPlugin(MavenContext context) {
        return context.getPlugin(QUARKUS_MAVEN_PLUGIN_ARTIFACT).flatMap(PluginContext::getVersion);
    }
}
