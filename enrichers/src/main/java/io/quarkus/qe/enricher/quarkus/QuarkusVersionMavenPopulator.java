package io.quarkus.qe.enricher.quarkus;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.StringUtils;

import io.quarkus.qe.enricher.maven.MavenContext;
import io.quarkus.qe.model.QuarkusExtension;
import io.quarkus.qe.model.Repository;

@ApplicationScoped
public class QuarkusVersionMavenPopulator extends BaseQuarkusMavenPopulator {

    @Override
    public void populate(Repository repository, MavenContext mavenContext) {
        String version = resolveVersionFromContext(mavenContext);
        if (StringUtils.isEmpty(version)) {
            version = mostPopularQuarkusExtVersion(repository);
        }

        repository.setQuarkusVersion(version);
    }

    private String mostPopularQuarkusExtVersion(Repository repository) {
        Map<String, Long> occurrences = repository.getExtensions().stream()
                .filter(hasVersion())
                .collect(Collectors.groupingBy(QuarkusExtension::getVersion, Collectors.counting()));

        if (occurrences.isEmpty()) {
            return StringUtils.EMPTY;
        }

        return Collections.max(occurrences.entrySet(), Comparator.comparingLong(Entry::getValue)).getKey();
    }

    private Predicate<QuarkusExtension> hasVersion() {
        return extension -> StringUtils.isNotEmpty(extension.getVersion());
    }

}
