package io.quarkus.qe.enricher.quarkus;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.qe.enricher.maven.DependencyContext;
import io.quarkus.qe.enricher.maven.MavenContext;
import io.quarkus.qe.model.QuarkusExtension;
import io.quarkus.qe.model.Repository;

@ApplicationScoped
public class QuarkusExtensionsMavenPopulator extends BaseQuarkusMavenPopulator {

    private static final String QUARKUS_TAG = "quarkus-";

    @Override
    public void populate(Repository repository, MavenContext mavenContext) {
        repository.setExtensions(mavenContext.getAllDependencies().stream()
                .filter(onlyQuarkusDependencies())
                .map(toQuarkusExtensionModel())
                .collect(Collectors.toSet()));
    }

    private Predicate<DependencyContext> onlyQuarkusDependencies() {
        return dependency -> dependency.getArtifactId().startsWith(QUARKUS_TAG);
    }

    private Function<DependencyContext, QuarkusExtension> toQuarkusExtensionModel() {
        return dependency -> {
            QuarkusExtension model = new QuarkusExtension();
            model.setName(dependency.getArtifactId());
            model.setVersion(dependency.getVersion().orElseGet(() -> resolveVersionFromContext(dependency.getContext())));
            return model;
        };
    }

}
