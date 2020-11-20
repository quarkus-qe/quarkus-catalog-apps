package io.quarkus.qe.data;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity(name = "repository")
public class RepositoryEntity extends PanacheEntity {
    @Column(unique = true, nullable = false)
    public String repoUrl;
    public String branch;
    public String name;
    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public Set<QuarkusExtensionEntity> extensions;
    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<LogEntity> logs;
}
