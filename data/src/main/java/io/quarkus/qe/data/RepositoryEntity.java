package io.quarkus.qe.data;

import static io.quarkus.qe.data.query.RepositoryQuery.FILTER_BRANCH;
import static io.quarkus.qe.data.query.RepositoryQuery.FILTER_RELATIVE_PATH;
import static io.quarkus.qe.data.query.RepositoryQuery.FILTER_REPO_URL;
import static io.quarkus.qe.data.query.RepositoryQuery.FILTER_VERSION;
import static io.quarkus.qe.data.query.RepositoryQuery.PARAM;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity(name = "repository")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "repoUrl", "branch", "relativePath" }))
@FilterDef(name = FILTER_BRANCH, parameters = { @ParamDef(name = PARAM, type = "string") })
@FilterDef(name = FILTER_RELATIVE_PATH, parameters = { @ParamDef(name = PARAM, type = "string") })
@FilterDef(name = FILTER_REPO_URL, parameters = { @ParamDef(name = PARAM, type = "string") })
@FilterDef(name = FILTER_VERSION, parameters = { @ParamDef(name = PARAM, type = "string") })
@Filter(name = FILTER_REPO_URL, condition = "repoUrl=:" + PARAM)
@Filter(name = FILTER_BRANCH, condition = "branch=:" + PARAM)
@Filter(name = FILTER_RELATIVE_PATH, condition = "relativePath=:" + PARAM)
@Filter(name = FILTER_VERSION, condition = "quarkus_version_id like :" + PARAM + " || '%'")
public class RepositoryEntity extends PanacheEntity {

    @Column(nullable = false)
    public String repoUrl;
    public String branch;
    public String relativePath;
    @Column(columnDefinition = "TIMESTAMP")
    public LocalDateTime createdAt;
    @Column(columnDefinition = "TIMESTAMP")
    public LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    public RepositoryStatus status;
    public String name;
    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public Set<QuarkusExtensionEntity> extensions;
    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public Set<LabelEntity> labels;
    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<LogEntity> logs;
    @ManyToOne
    @JoinColumn(name = "quarkus_version_id")
    public QuarkusVersionEntity quarkusVersion;

    public static final Predicate<RepositoryEntity> byAnyExtensionWithNameAndVersionIfNotEmpty(String name, String version) {
        return repository -> repository.extensions.stream()
                .anyMatch(QuarkusExtensionEntity.byNameAndVersionIfNotEmpty(name, version));
    }

    public static final Predicate<RepositoryEntity> byAnyLabel(String label) {
        return repository -> repository.labels.stream().anyMatch(l -> l.name.equals(label));
    }
}
