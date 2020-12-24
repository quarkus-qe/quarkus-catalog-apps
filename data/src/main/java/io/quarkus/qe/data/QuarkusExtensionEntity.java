package io.quarkus.qe.data;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity(name = "quarkus_extension")
public class QuarkusExtensionEntity extends PanacheEntity {
    @ManyToOne
    @JoinColumn(name = "repository_id", nullable = false)
    public RepositoryEntity repository;
    @Column(nullable = false)
    public String name;
    @ManyToOne
    @JoinColumn(name = "quarkus_version_id")
    public QuarkusVersionEntity version;

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof QuarkusExtensionEntity))
            return false;
        QuarkusExtensionEntity other = (QuarkusExtensionEntity) obj;
        return name.equals(other.name) && Objects.equals(other.version, version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }

    @Override
    public String toString() {
        return name + Optional.ofNullable(version).map(v -> ":" + v).orElse(EMPTY);
    }

    public static final Predicate<QuarkusExtensionEntity> byNameAndVersionIfNotEmpty(String name, String version) {
        return byNameIfNotEmpty(name).and(byVersionIfNotEmpty(version));
    }

    public static final Predicate<QuarkusExtensionEntity> byNameIfNotEmpty(String name) {
        return extension -> isEmpty(name) || extension.name.equals(name);
    }

    public static final Predicate<QuarkusExtensionEntity> byVersionIfNotEmpty(String version) {
        return extension -> isEmpty(version) || (extension.version != null && extension.version.id.startsWith(version));
    }
}
